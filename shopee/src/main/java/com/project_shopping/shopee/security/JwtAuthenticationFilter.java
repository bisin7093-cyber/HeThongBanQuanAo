package com.project_shopping.shopee.security;

import com.project_shopping.shopee.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
 private final JwtService jwtService; private final UserRepository users;
 public JwtAuthenticationFilter(JwtService jwtService, UserRepository users) { this.jwtService=jwtService; this.users=users; }
 @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
  String header=request.getHeader("Authorization");
  if (header != null && header.startsWith("Bearer ") && SecurityContextHolder.getContext().getAuthentication()==null) {
   try {
    String email=jwtService.extractSubject(header.substring(7));
    users.findByEmailIgnoreCase(email).ifPresent(account -> {
     var principal=User.withUsername(account.getEmail()).password(account.getPasswordHash()).roles(account.getRole().name()).build();
     var auth=new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
     auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request)); SecurityContextHolder.getContext().setAuthentication(auth);
    });
   } catch (JwtException | IllegalArgumentException ignored) { SecurityContextHolder.clearContext(); }
  }
  chain.doFilter(request,response);
 }
}
