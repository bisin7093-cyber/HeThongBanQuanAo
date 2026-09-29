package com.project_shopping.shopee.controller;

import com.project_shopping.shopee.dto.ApiDtos.*;
import com.project_shopping.shopee.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController @RequestMapping("/api/products")
public class ProductController {
 private final ProductService products;
 public ProductController(ProductService products) { this.products=products; }
 @GetMapping public ProductPageResponse list(
  @RequestParam(required=false) String keyword,
  @RequestParam(required=false) String category,
  @RequestParam(required=false) String size,
  @RequestParam(required=false) String color,
  @RequestParam(required=false) BigDecimal minPrice,
  @RequestParam(required=false) BigDecimal maxPrice,
  @RequestParam(required=false) Boolean inStock,
  @RequestParam(defaultValue="0") int page,
  @RequestParam(defaultValue="12") int pageSize,
  @RequestParam(defaultValue="newest") String sort) {
  if(page<0 || pageSize<1 || pageSize>100) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST,"page must be >= 0 and pageSize must be between 1 and 100");
  return products.search(keyword,category,size,color,minPrice,maxPrice,inStock,page,pageSize,sort);
 }
 @GetMapping("/filters") public ProductFilterOptions filters() { return products.filterOptions(); }
 @GetMapping("/categories") public List<String> categories() { return products.categories(); }
 @GetMapping("/{id}") public ProductResponse detail(@PathVariable Long id) { return products.getActive(id); }
 @GetMapping("/{id}/related") public List<ProductResponse> related(@PathVariable Long id,@RequestParam(defaultValue="8") int limit) { return products.related(id,limit); }
}
