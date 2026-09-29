package com.project_shopping.shopee.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

@Entity @Table(name = "users") @Getter @Setter @NoArgsConstructor
public class AppUser {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
	@Column(name = "full_name", nullable = false, length = 120) private String fullName;
	@Column(nullable = false, unique = true, length = 190) private String email;
	@Column(name = "password_hash", nullable = false, length = 100) private String passwordHash;
	@Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private Role role = Role.USER;
	@Column(length = 30) private String phone;
	@Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt = Instant.now();
	public AppUser(String fullName, String email, String passwordHash, Role role, String phone) { this.fullName=fullName; this.email=email; this.passwordHash=passwordHash; this.role=role; this.phone=phone; }
}
