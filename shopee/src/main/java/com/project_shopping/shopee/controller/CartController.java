package com.project_shopping.shopee.controller;

import com.project_shopping.shopee.dto.ApiDtos.*;
import com.project_shopping.shopee.service.CartService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/cart")
public class CartController {
 private final CartService carts;
 public CartController(CartService carts) { this.carts=carts; }
 @GetMapping public CartResponse get(@AuthenticationPrincipal UserDetails user) { return carts.get(user.getUsername()); }
 @PostMapping("/items") @ResponseStatus(HttpStatus.CREATED) public CartResponse add(@AuthenticationPrincipal UserDetails user,@Valid @RequestBody CartItemRequest request) { return carts.add(user.getUsername(),request); }
 @PutMapping("/items/{id}") public CartResponse update(@AuthenticationPrincipal UserDetails user,@PathVariable Long id,@Valid @RequestBody CartQuantityRequest request) { return carts.update(user.getUsername(),id,request); }
 @DeleteMapping("/items/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@AuthenticationPrincipal UserDetails user,@PathVariable Long id) { carts.delete(user.getUsername(),id); }
}
