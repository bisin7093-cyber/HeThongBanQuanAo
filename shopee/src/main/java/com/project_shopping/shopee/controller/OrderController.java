package com.project_shopping.shopee.controller;

import com.project_shopping.shopee.dto.ApiDtos.*;
import com.project_shopping.shopee.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/orders")
public class OrderController {
 private final OrderService orders;
 public OrderController(OrderService orders) { this.orders=orders; }
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public OrderResponse checkout(@AuthenticationPrincipal UserDetails user,@Valid @RequestBody CheckoutRequest request) { return orders.checkout(user.getUsername(),request); }
 @GetMapping public List<OrderResponse> mine(@AuthenticationPrincipal UserDetails user) { return orders.myOrders(user.getUsername()); }
 @GetMapping("/{id}") public OrderResponse detail(@AuthenticationPrincipal UserDetails user,@PathVariable Long id) { return orders.myOrder(user.getUsername(),id); }
}
