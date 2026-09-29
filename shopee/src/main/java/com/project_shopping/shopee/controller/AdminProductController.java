package com.project_shopping.shopee.controller;

import com.project_shopping.shopee.dto.ApiDtos.*;
import com.project_shopping.shopee.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/admin/products")
public class AdminProductController {
 private final ProductService products;
 public AdminProductController(ProductService products) { this.products=products; }
 @GetMapping public List<ProductResponse> list() { return products.listAll(); }
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public ProductResponse create(@Valid @RequestBody ProductRequest request) { return products.create(request); }
 @PutMapping("/{id}") public ProductResponse update(@PathVariable Long id,@Valid @RequestBody ProductRequest request) { return products.update(id,request); }
 @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void deactivate(@PathVariable Long id) { products.deactivate(id); }
 @PostMapping("/{productId}/variants") @ResponseStatus(HttpStatus.CREATED) public VariantResponse addVariant(@PathVariable Long productId,@Valid @RequestBody VariantRequest request) { return products.addVariant(productId,request); }
 @PutMapping("/{productId}/variants/{variantId}") public VariantResponse updateVariant(@PathVariable Long productId,@PathVariable Long variantId,@Valid @RequestBody VariantRequest request) { return products.updateVariant(productId,variantId,request); }
 @DeleteMapping("/{productId}/variants/{variantId}") @ResponseStatus(HttpStatus.NO_CONTENT) public void deleteVariant(@PathVariable Long productId,@PathVariable Long variantId) { products.deleteVariant(productId,variantId); }
}
