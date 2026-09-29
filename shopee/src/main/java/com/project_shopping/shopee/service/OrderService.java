package com.project_shopping.shopee.service;

import com.project_shopping.shopee.dto.ApiDtos.*;
import com.project_shopping.shopee.model.*;
import com.project_shopping.shopee.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {
 private final OrderRepository orders; private final UserRepository users; private final CartRepository carts; private final VariantRepository variants; private final NotificationRepository notifications;
 public OrderService(OrderRepository orders,UserRepository users,CartRepository carts,VariantRepository variants,NotificationRepository notifications) { this.orders=orders; this.users=users; this.carts=carts; this.variants=variants; this.notifications=notifications; }
 @Transactional public OrderResponse checkout(String email,CheckoutRequest request) {
  AppUser user=user(email); Cart cart=carts.findByUserIdForUpdate(user.getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,"Cart is empty"));
  if(cart.getItems().isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Cart is empty");
  Map<Long,ProductVariant> locked=new TreeMap<>();
  for(CartItem item:cart.getItems().stream().sorted(Comparator.comparing(i->i.getVariant().getId())).toList()) {
   ProductVariant variant=variants.findByIdForUpdate(item.getVariant().getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,"A cart variant no longer exists"));
   if(variant.getProduct().getStatus()!=ProductStatus.ACTIVE) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Cart contains an inactive product");
   if(item.getQuantity()>variant.getStockQuantity()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Insufficient stock for variant " + variant.getId());
   locked.put(variant.getId(),variant);
  }
  BigDecimal total=cart.getItems().stream().map(i -> locked.get(i.getVariant().getId()).getPrice().multiply(BigDecimal.valueOf(i.getQuantity()))).reduce(BigDecimal.ZERO,BigDecimal::add);
  CustomerOrder order=new CustomerOrder(user,request.shippingAddress().trim(),request.phone().trim(),total);
  for(CartItem item:cart.getItems()) { ProductVariant variant=locked.get(item.getVariant().getId()); BigDecimal price=variant.getPrice(); order.getItems().add(new OrderItem(order,variant,item.getQuantity(),price)); variant.setStockQuantity(variant.getStockQuantity()-item.getQuantity()); }
  Payment payment=new Payment(order,request.paymentMethod(),total); order.setPayment(payment);
  CustomerOrder saved=orders.save(order);
  cart.getItems().clear(); cart.setUpdatedAt(Instant.now());
  return toResponse(saved);
 }
 @Transactional(readOnly=true) public List<OrderResponse> myOrders(String email) { AppUser user=user(email); return orders.findAllByUserIdOrderByCreatedAtDesc(user.getId()).stream().map(this::toResponse).toList(); }
 @Transactional(readOnly=true) public OrderResponse myOrder(String email,Long id) { AppUser user=user(email); return orders.findByIdAndUserId(id,user.getId()).map(this::toResponse).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Order not found")); }
 @Transactional(readOnly=true) public List<OrderResponse> allOrders() { return orders.findAllByOrderByCreatedAtDesc().stream().map(this::toResponse).toList(); }
 @Transactional(readOnly=true) public OrderResponse adminOrder(Long id) { return orders.findDetailedById(id).map(this::toResponse).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Order not found")); }
 @Transactional public OrderResponse confirm(Long id) { CustomerOrder order=lockOrder(id); requirePending(order); order.setStatus(OrderStatus.CONFIRMED); notifications.save(new Notification(order.getUser(),order,"Order confirmed","Order #"+order.getId()+" has been confirmed.")); return toResponse(order); }
 @Transactional public OrderResponse cancel(Long id) {
  CustomerOrder order=lockOrder(id); requirePending(order);
  Map<Long,ProductVariant> locked=new TreeMap<>();
  for(OrderItem line:order.getItems().stream().sorted(Comparator.comparing(i->i.getVariant().getId())).toList()) {
   ProductVariant variant=variants.findByIdForUpdate(line.getVariant().getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,"An order variant no longer exists"));
   locked.put(variant.getId(),variant);
  }
  for(OrderItem line:order.getItems()) { ProductVariant variant=locked.get(line.getVariant().getId()); variant.setStockQuantity(Math.addExact(variant.getStockQuantity(),line.getQuantity())); }
  order.setStatus(OrderStatus.CANCELLED); notifications.save(new Notification(order.getUser(),order,"Order cancelled","Order #"+order.getId()+" has been cancelled.")); return toResponse(order);
 }
 private CustomerOrder lockOrder(Long id) { return orders.findByIdForUpdate(id).map(o -> orders.findDetailedById(id).orElse(o)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Order not found")); }
 private void requirePending(CustomerOrder order) { if(order.getStatus()!=OrderStatus.PENDING) throw new ResponseStatusException(HttpStatus.CONFLICT,"Only PENDING orders can be confirmed or cancelled"); }
 private AppUser user(String email) { return users.findByEmailIgnoreCase(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,"User not found")); }
 private OrderResponse toResponse(CustomerOrder o) {
  var lines=o.getItems().stream().map(i -> new OrderLineResponse(i.getId(),i.getVariant().getId(),i.getVariant().getProduct().getId(),i.getVariant().getProduct().getName(),i.getVariant().getSize(),i.getVariant().getColor(),i.getQuantity(),i.getUnitPrice(),i.getSubTotal())).toList();
  Payment p=o.getPayment(); PaymentResponse payment=p==null?null:new PaymentResponse(p.getPaymentMethod(),p.getPaymentStatus(),p.getAmount(),p.getPaidAt());
  return new OrderResponse(o.getId(),o.getUser().getId(),o.getShippingAddress(),o.getPhone(),o.getTotalAmount(),o.getStatus(),o.getCreatedAt(),lines,payment);
 }
}
