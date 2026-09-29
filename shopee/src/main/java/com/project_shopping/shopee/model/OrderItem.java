package com.project_shopping.shopee.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Entity @Table(name = "order_items") @Getter @Setter @NoArgsConstructor
public class OrderItem {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
	@ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "order_id", nullable = false) private CustomerOrder order;
	@ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "variant_id", nullable = false) private ProductVariant variant;
	@Column(nullable = false) private int quantity;
	@Column(name = "unit_price", nullable = false, precision = 12, scale = 2) private BigDecimal unitPrice;
	@Column(name = "sub_total", nullable = false, precision = 14, scale = 2) private BigDecimal subTotal;
	public OrderItem(CustomerOrder order, ProductVariant variant, int quantity, BigDecimal unitPrice) { this.order=order; this.variant=variant; this.quantity=quantity; this.unitPrice=unitPrice; this.subTotal=unitPrice.multiply(BigDecimal.valueOf(quantity)); }
}
