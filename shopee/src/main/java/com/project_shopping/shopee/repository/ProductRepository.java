package com.project_shopping.shopee.repository;
import com.project_shopping.shopee.model.Product;
import com.project_shopping.shopee.model.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
 @EntityGraph(attributePaths = "variants")
 List<Product> findByStatus(ProductStatus status);
 @EntityGraph(attributePaths = "variants") Optional<Product> findWithVariantsByIdAndStatus(Long id, ProductStatus status);
 @EntityGraph(attributePaths = "variants") Optional<Product> findWithVariantsById(Long id);
 List<Product> findByStatusAndCategoryIgnoreCaseAndIdNotOrderByCreatedAtDesc(ProductStatus status,String category,Long id);
 @Query("select distinct p.category from Product p where p.status = :status order by p.category") List<String> findDistinctCategories(@Param("status") ProductStatus status);
 @Query("select distinct v.size from ProductVariant v where v.product.status = :status and v.stockQuantity > 0 order by v.size") List<String> findAvailableSizes(@Param("status") ProductStatus status);
 @Query("select distinct v.color from ProductVariant v where v.product.status = :status and v.stockQuantity > 0 order by v.color") List<String> findAvailableColors(@Param("status") ProductStatus status);
 @Query("select min(p.price) from Product p where p.status = :status and exists (select v.id from ProductVariant v where v.product = p and v.stockQuantity > 0)") java.math.BigDecimal findMinAvailablePrice(@Param("status") ProductStatus status);
 @Query("select max(p.price) from Product p where p.status = :status and exists (select v.id from ProductVariant v where v.product = p and v.stockQuantity > 0)") java.math.BigDecimal findMaxAvailablePrice(@Param("status") ProductStatus status);
}
