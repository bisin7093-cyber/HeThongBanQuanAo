package com.project_shopping.shopee.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name = "orders") @Getter @Setter @NoArgsConstructor
public class CustomerOrder {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
	@ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false) private AppUser user;
	@Column(name = "shipping_address", nullable = false, length = 500) private String shippingAddress;
	@Column(nullable = false, length = 30) private String phone;
	@Column(name = "total_amount", nullable = false, precision = 14, scale = 2) private BigDecimal totalAmount;
	@Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private OrderStatus status = OrderStatus.PENDING;
	@Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt = Instant.now();
	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true) private List<OrderItem> items = new ArrayList<>();
	@OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true) private Payment payment;
	public CustomerOrder(AppUser user, String shippingAddress, String phone, BigDecimal totalAmount) { this.user=user; this.shippingAddress=shippingAddress; this.phone=phone; this.totalAmount=totalAmount; }
}
