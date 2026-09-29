package com.project_shopping.shopee.service;

import com.project_shopping.shopee.dto.ApiDtos.*;
import com.project_shopping.shopee.model.*;
import com.project_shopping.shopee.repository.CartRepository;
import com.project_shopping.shopee.repository.UserRepository;
import com.project_shopping.shopee.repository.VariantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.Instant;

@Service
public class CartService {
 private final CartRepository carts; private final UserRepository users; private final VariantRepository variants;
 public CartService(CartRepository carts,UserRepository users,VariantRepository variants) { this.carts=carts; this.users=users; this.variants=variants; }
 @Transactional public CartResponse get(String email) { AppUser user=user(email); return view(cart(user)); }
 @Transactional public CartResponse add(String email,CartItemRequest request) {
  AppUser user=user(email); Cart cart=cart(user); ProductVariant variant=variants.findByIdForUpdate(request.variantId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Variant not found"));
  if(variant.getProduct().getStatus()!=ProductStatus.ACTIVE) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Inactive products cannot be added to the cart");
  CartItem line=cart.getItems().stream().filter(i -> i.getVariant().getId().equals(variant.getId())).findFirst().orElse(null);
  int newQuantity=request.quantity()+(line==null?0:line.getQuantity());
  if(newQuantity>variant.getStockQuantity()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Requested quantity exceeds available stock");
  if(line==null) cart.getItems().add(new CartItem(cart,variant,newQuantity)); else line.setQuantity(newQuantity);
  cart.setUpdatedAt(Instant.now()); return view(carts.save(cart));
 }
 @Transactional public CartResponse update(String email,Long itemId,CartQuantityRequest request) {
  Cart cart=ownedCart(email); CartItem line=findLine(cart,itemId); ProductVariant variant=variants.findByIdForUpdate(line.getVariant().getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Variant not found"));
  if(variant.getProduct().getStatus()!=ProductStatus.ACTIVE) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Inactive products cannot remain in the cart");
  if(request.quantity()>variant.getStockQuantity()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Requested quantity exceeds available stock");
  line.setQuantity(request.quantity()); cart.setUpdatedAt(Instant.now()); return view(cart);
 }
 @Transactional public void delete(String email,Long itemId) { Cart cart=ownedCart(email); CartItem line=findLine(cart,itemId); cart.getItems().remove(line); cart.setUpdatedAt(Instant.now()); }
 private AppUser user(String email) { return users.findByEmailIgnoreCase(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,"User not found")); }
 private Cart cart(AppUser user) { return carts.findByUserId(user.getId()).orElseGet(() -> carts.save(new Cart(user))); }
 private Cart ownedCart(String email) { AppUser user=user(email); return carts.findByUserId(user.getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Cart not found")); }
 private CartItem findLine(Cart cart,Long id) { return cart.getItems().stream().filter(i -> i.getId().equals(id)).findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Cart item not found")); }
 private CartResponse view(Cart cart) {
  var lines=cart.getItems().stream().map(i -> { var v=i.getVariant(); var p=v.getProduct(); var subtotal=v.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())); return new CartLineResponse(i.getId(),v.getId(),p.getId(),p.getName(),v.getSize(),v.getColor(),v.getPrice(),i.getQuantity(),subtotal,v.getStockQuantity()); }).toList();
  BigDecimal total=lines.stream().map(CartLineResponse::subTotal).reduce(BigDecimal.ZERO,BigDecimal::add);
  return new CartResponse(cart.getId(),lines,total);
 }
}
