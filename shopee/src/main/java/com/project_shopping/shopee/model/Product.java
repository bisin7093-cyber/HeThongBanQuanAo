package com.project_shopping.shopee.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name = "products") @Getter @Setter @NoArgsConstructor
public class Product {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
	@Column(nullable = false, length = 180) private String name;
	@Column(nullable = false, length = 80) private String category = "Thời trang";
	@Column(columnDefinition = "TEXT") private String description;
	@Column(nullable = false, precision = 12, scale = 2) private BigDecimal price;
	@Column(name = "image_url", length = 1000) private String imageUrl;
	@Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private ProductStatus status = ProductStatus.ACTIVE;
	@Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt = Instant.now();
	@OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true) private List<ProductVariant> variants = new ArrayList<>();
	public Product(String name, String description, BigDecimal price, String imageUrl) { this.name=name; this.description=description; this.price=price; this.imageUrl=imageUrl; }
}
