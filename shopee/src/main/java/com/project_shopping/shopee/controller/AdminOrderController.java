package com.project_shopping.shopee.controller;

import com.project_shopping.shopee.dto.ApiDtos.OrderResponse;
import com.project_shopping.shopee.service.OrderService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/admin/orders")
public class AdminOrderController {
 private final OrderService orders;
 public AdminOrderController(OrderService orders) { this.orders=orders; }
 @GetMapping public List<OrderResponse> list() { return orders.allOrders(); }
 @GetMapping("/{id}") public OrderResponse detail(@PathVariable Long id) { return orders.adminOrder(id); }
 @PutMapping("/{id}/confirm") public OrderResponse confirm(@PathVariable Long id) { return orders.confirm(id); }
 @PutMapping("/{id}/cancel") public OrderResponse cancel(@PathVariable Long id) { return orders.cancel(id); }
}
