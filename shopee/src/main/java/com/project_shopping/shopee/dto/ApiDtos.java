package com.project_shopping.shopee.dto;

import com.project_shopping.shopee.model.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class ApiDtos {
 private ApiDtos() {}
 public record RegisterRequest(@NotBlank @Size(max=120) String fullName, @NotBlank @Email @Size(max=190) String email, @NotBlank String password, @Size(max=30) String phone) {}
 public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}
 public record AuthResponse(String accessToken, Long userId, Role role) {}
 public record UserResponse(Long id, String fullName, String email, Role role, String phone) {}
 public record ProductRequest(@NotBlank @Size(max=180) String name, @Size(max=80) String category, String description, @NotNull @DecimalMin(value="0.01") BigDecimal price, @Size(max=1000) String imageUrl, ProductStatus status) {}
 public record VariantRequest(@NotBlank @Size(max=40) String size, @NotBlank @Size(max=60) String color, @NotNull @PositiveOrZero Integer stockQuantity, @NotNull @DecimalMin(value="0.01") BigDecimal price) {}
 public record VariantResponse(Long id, String size, String color, int stockQuantity, BigDecimal price) {}
 public record ProductResponse(Long id, String name, String category, String description, BigDecimal price, String imageUrl, ProductStatus status, Instant createdAt, List<VariantResponse> variants) {}
 public record ProductPageResponse(List<ProductResponse> items, int page, int size, long totalElements, int totalPages, boolean first, boolean last) {}
 public record ProductFilterOptions(List<String> categories, List<String> sizes, List<String> colors, BigDecimal minPrice, BigDecimal maxPrice) {}
 public record CartItemRequest(@NotNull @Positive Long variantId, @NotNull @Positive Integer quantity) {}
 public record CartQuantityRequest(@NotNull @Positive Integer quantity) {}
 public record CartLineResponse(Long id, Long variantId, Long productId, String productName, String size, String color, BigDecimal unitPrice, int quantity, BigDecimal subTotal, int availableStock) {}
 public record CartResponse(Long id, List<CartLineResponse> items, BigDecimal totalAmount) {}
 public record CheckoutRequest(@NotBlank @Size(max=500) String shippingAddress, @NotBlank @Size(max=30) String phone, @NotNull PaymentMethod paymentMethod) {}
 public record OrderLineResponse(Long id, Long variantId, Long productId, String productName, String size, String color, int quantity, BigDecimal unitPrice, BigDecimal subTotal) {}
 public record PaymentResponse(PaymentMethod paymentMethod, PaymentStatus paymentStatus, BigDecimal amount, Instant paidAt) {}
 public record OrderResponse(Long id, Long userId, String shippingAddress, String phone, BigDecimal totalAmount, OrderStatus status, Instant createdAt, List<OrderLineResponse> items, PaymentResponse payment) {}
 public record NotificationResponse(Long id, Long orderId, String title, String message, NotificationType type, boolean read, Instant createdAt) {}
 public record ErrorResponse(Instant timestamp, int status, String error, String message) {}
}
