package com.project_shopping.shopee.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name = "carts") @Getter @Setter @NoArgsConstructor
public class Cart {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
	@OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false, unique = true) private AppUser user;
	@Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt = Instant.now();
	@Column(name = "updated_at", nullable = false) private Instant updatedAt = Instant.now();
	@OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true) private List<CartItem> items = new ArrayList<>();
	public Cart(AppUser user) { this.user=user; }
}
