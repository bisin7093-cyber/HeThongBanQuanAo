package com.project_shopping.shopee.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity @Table(name = "cart_items", uniqueConstraints = @UniqueConstraint(columnNames = {"cart_id", "variant_id"})) @Getter @Setter @NoArgsConstructor
public class CartItem {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
	@ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "cart_id", nullable = false) private Cart cart;
	@ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "variant_id", nullable = false) private ProductVariant variant;
	@Column(nullable = false) private int quantity;
	public CartItem(Cart cart, ProductVariant variant, int quantity) { this.cart=cart; this.variant=variant; this.quantity=quantity; }
}
