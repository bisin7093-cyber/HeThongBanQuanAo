package com.project_shopping.shopee.controller;

import com.project_shopping.shopee.dto.ApiDtos.NotificationResponse;
import com.project_shopping.shopee.service.NotificationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/notifications")
public class NotificationController {
 private final NotificationService notifications;
 public NotificationController(NotificationService notifications) { this.notifications=notifications; }
 @GetMapping public List<NotificationResponse> list(@AuthenticationPrincipal UserDetails user) { return notifications.list(user.getUsername()); }
 @PutMapping("/{id}/read") public NotificationResponse markRead(@AuthenticationPrincipal UserDetails user,@PathVariable Long id) { return notifications.markRead(user.getUsername(),id); }
}
