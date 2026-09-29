package com.project_shopping.shopee.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;

@Entity @Table(name = "payments") @Getter @Setter @NoArgsConstructor
public class Payment {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
	@OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "order_id", nullable = false, unique = true) private CustomerOrder order;
	@Enumerated(EnumType.STRING) @Column(name = "payment_method", nullable = false, length = 20) private PaymentMethod paymentMethod;
	@Enumerated(EnumType.STRING) @Column(name = "payment_status", nullable = false, length = 20) private PaymentStatus paymentStatus = PaymentStatus.PENDING;
	@Column(nullable = false, precision = 14, scale = 2) private BigDecimal amount;
	@Column(name = "paid_at") private Instant paidAt;
	public Payment(CustomerOrder order, PaymentMethod paymentMethod, BigDecimal amount) { this.order=order; this.paymentMethod=paymentMethod; this.amount=amount; }
}
