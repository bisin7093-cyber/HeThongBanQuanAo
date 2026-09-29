package com.project_shopping.shopee.config;

import com.project_shopping.shopee.model.AppUser;
import com.project_shopping.shopee.model.Role;
import com.project_shopping.shopee.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminBootstrap implements ApplicationRunner {
 private final UserRepository users; private final PasswordEncoder encoder; private final String email; private final String password;
 public AdminBootstrap(UserRepository users, PasswordEncoder encoder, @Value("${app.bootstrap-admin.email:}") String email, @Value("${app.bootstrap-admin.password:}") String password) { this.users=users; this.encoder=encoder; this.email=email; this.password=password; }
 @Override @Transactional public void run(ApplicationArguments args) {
  if(email.isBlank() && password.isBlank()) return;
  if(email.isBlank() || password.isBlank()) throw new IllegalStateException("Set both ADMIN_EMAIL and ADMIN_PASSWORD to bootstrap an admin account");
  String normalized=email.trim().toLowerCase();
  var existing=users.findByEmailIgnoreCase(normalized);
  if(existing.isPresent()) {
   if(existing.get().getRole()!=Role.ADMIN) throw new IllegalStateException("Configured ADMIN_EMAIL is already used by a non-admin account");
   return;
  }
  users.save(new AppUser("Administrator", normalized, encoder.encode(password), Role.ADMIN, null));
 }
}
