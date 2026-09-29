package com.project_shopping.shopee.repository;

import com.project_shopping.shopee.model.Product;
import com.project_shopping.shopee.model.ProductStatus;
import com.project_shopping.shopee.model.ProductVariant;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class ProductSpecifications {
 private ProductSpecifications() {}
 public static Specification<Product> catalog(String keyword,String category,String size,String color,BigDecimal minPrice,BigDecimal maxPrice,Boolean inStock) {
  return (root,query,cb) -> {
   List<Predicate> predicates=new ArrayList<>();
   predicates.add(cb.equal(root.get("status"),ProductStatus.ACTIVE));
   if(keyword!=null && !keyword.isBlank()) {
    String term="%"+keyword.trim().toLowerCase().replace("%","\\%").replace("_","\\_")+"%";
    predicates.add(cb.or(cb.like(cb.lower(root.get("name")),term,'\\'),cb.like(cb.lower(root.get("description")),term,'\\')));
   }
   if(category!=null && !category.isBlank()) predicates.add(cb.equal(root.get("category"),category.trim()));
   if(minPrice!=null) predicates.add(cb.greaterThanOrEqualTo(root.get("price"),minPrice));
   if(maxPrice!=null) predicates.add(cb.lessThanOrEqualTo(root.get("price"),maxPrice));
   boolean variantFilter=(size!=null&&!size.isBlank())||(color!=null&&!color.isBlank())||inStock!=null;
   if(variantFilter) {
    Subquery<Integer> subquery=query.subquery(Integer.class); var variant=subquery.from(ProductVariant.class);
    List<Predicate> matches=new ArrayList<>(); matches.add(cb.equal(variant.get("product").get("id"),root.get("id")));
    if(size!=null&&!size.isBlank()) matches.add(cb.equal(cb.lower(variant.get("size")),size.trim().toLowerCase()));
    if(color!=null&&!color.isBlank()) matches.add(cb.equal(cb.lower(variant.get("color")),color.trim().toLowerCase()));
    if(inStock!=null) matches.add(inStock?cb.greaterThan(variant.get("stockQuantity"),0):cb.equal(variant.get("stockQuantity"),0));
    subquery.select(cb.literal(1)).where(matches.toArray(Predicate[]::new)); predicates.add(cb.exists(subquery));
   }
   return cb.and(predicates.toArray(Predicate[]::new));
  };
 }
}
