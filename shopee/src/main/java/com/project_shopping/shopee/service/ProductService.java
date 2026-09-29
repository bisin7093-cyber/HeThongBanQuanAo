package com.project_shopping.shopee.service;

import com.project_shopping.shopee.dto.ApiDtos.*;
import com.project_shopping.shopee.model.*;
import com.project_shopping.shopee.repository.ProductRepository;
import com.project_shopping.shopee.repository.ProductSpecifications;
import com.project_shopping.shopee.repository.VariantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.math.BigDecimal;

@Service
public class ProductService {
 private final ProductRepository products; private final VariantRepository variants;
 public ProductService(ProductRepository products, VariantRepository variants) { this.products=products; this.variants=variants; }
 @Transactional(readOnly=true) public List<String> categories() { return products.findDistinctCategories(ProductStatus.ACTIVE); }
 @Transactional(readOnly=true) public ProductPageResponse search(String keyword,String category,String size,String color,BigDecimal minPrice,BigDecimal maxPrice,Boolean inStock,int page,int pageSize,String sortKey) {
  if(minPrice!=null && minPrice.signum()<0 || maxPrice!=null && maxPrice.signum()<0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Price filters must be zero or greater");
  if(minPrice!=null && maxPrice!=null && minPrice.compareTo(maxPrice)>0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"minPrice must not be greater than maxPrice");
  Sort sort=switch(sortKey==null?"newest":sortKey.toLowerCase()) {
   case "newest" -> Sort.by(Sort.Direction.DESC,"createdAt");
   case "price_asc" -> Sort.by(Sort.Direction.ASC,"price");
   case "price_desc" -> Sort.by(Sort.Direction.DESC,"price");
   case "name_asc" -> Sort.by(Sort.Direction.ASC,"name");
   case "name_desc" -> Sort.by(Sort.Direction.DESC,"name");
   default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"sort must be newest, price_asc, price_desc, name_asc, or name_desc");
  };
  Page<Product> result=products.findAll(ProductSpecifications.catalog(keyword,category,size,color,minPrice,maxPrice,inStock),PageRequest.of(page,pageSize,sort));
  return new ProductPageResponse(result.getContent().stream().map(this::toResponse).toList(),result.getNumber(),result.getSize(),result.getTotalElements(),result.getTotalPages(),result.isFirst(),result.isLast());
 }
 @Transactional(readOnly=true) public ProductFilterOptions filterOptions() { return new ProductFilterOptions(products.findDistinctCategories(ProductStatus.ACTIVE),products.findAvailableSizes(ProductStatus.ACTIVE),products.findAvailableColors(ProductStatus.ACTIVE),products.findMinAvailablePrice(ProductStatus.ACTIVE),products.findMaxAvailablePrice(ProductStatus.ACTIVE)); }
 @Transactional(readOnly=true) public List<ProductResponse> related(Long id,int limit) {
  Product product=products.findWithVariantsByIdAndStatus(id,ProductStatus.ACTIVE).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Product not found"));
  if(limit<1 || limit>20) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"limit must be between 1 and 20");
  return products.findByStatusAndCategoryIgnoreCaseAndIdNotOrderByCreatedAtDesc(ProductStatus.ACTIVE,product.getCategory(),id).stream().limit(limit).map(this::toResponse).toList();
 }
 @Transactional(readOnly=true) public ProductResponse getActive(Long id) { return toResponse(products.findWithVariantsByIdAndStatus(id,ProductStatus.ACTIVE).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Product not found"))); }
 @Transactional(readOnly=true) public List<ProductResponse> listAll() { return products.findAll().stream().map(this::toResponse).toList(); }
 @Transactional public ProductResponse create(ProductRequest request) {
  Product product=new Product(request.name().trim(),request.description(),request.price(),request.imageUrl());
  if(request.category()!=null && !request.category().isBlank()) product.setCategory(request.category().trim());
  if(request.status()!=null) product.setStatus(request.status());
  return toResponse(products.save(product));
 }
 @Transactional public ProductResponse update(Long id, ProductRequest request) {
  Product product=products.findWithVariantsById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Product not found"));
  product.setName(request.name().trim()); product.setDescription(request.description()); product.setPrice(request.price()); product.setImageUrl(request.imageUrl());
  if(request.category()!=null && !request.category().isBlank()) product.setCategory(request.category().trim());
  if(request.status()!=null) product.setStatus(request.status());
  return toResponse(product);
 }
 @Transactional public void deactivate(Long id) {
  Product product=products.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Product not found")); product.setStatus(ProductStatus.INACTIVE);
 }
 @Transactional public VariantResponse addVariant(Long productId, VariantRequest request) {
  Product product=products.findById(productId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Product not found"));
  return toVariant(variants.save(new ProductVariant(product,request.size().trim(),request.color().trim(),request.stockQuantity(),request.price())));
 }
 @Transactional public VariantResponse updateVariant(Long productId, Long variantId, VariantRequest request) {
  ProductVariant variant=variants.findById(variantId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Variant not found"));
  if(!variant.getProduct().getId().equals(productId)) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Variant not found");
  variant.setSize(request.size().trim()); variant.setColor(request.color().trim()); variant.setStockQuantity(request.stockQuantity()); variant.setPrice(request.price()); return toVariant(variant);
 }
 @Transactional public void deleteVariant(Long productId, Long variantId) {
  ProductVariant variant=variants.findById(variantId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Variant not found"));
  if(!variant.getProduct().getId().equals(productId)) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Variant not found");
  try { variants.delete(variant); variants.flush(); }
  catch (RuntimeException ex) { throw new ResponseStatusException(HttpStatus.CONFLICT,"Variant is referenced by a cart or order and cannot be deleted; set its stock to zero instead",ex); }
 }
 private ProductResponse toResponse(Product p) { return new ProductResponse(p.getId(),p.getName(),p.getCategory(),p.getDescription(),p.getPrice(),p.getImageUrl(),p.getStatus(),p.getCreatedAt(),p.getVariants().stream().map(this::toVariant).toList()); }
 private VariantResponse toVariant(ProductVariant v) { return new VariantResponse(v.getId(),v.getSize(),v.getColor(),v.getStockQuantity(),v.getPrice()); }
}
