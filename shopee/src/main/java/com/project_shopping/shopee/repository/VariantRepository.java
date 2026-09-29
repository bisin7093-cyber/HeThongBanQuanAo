package com.project_shopping.shopee.repository;
import com.project_shopping.shopee.model.ProductVariant;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
public interface VariantRepository extends JpaRepository<ProductVariant, Long> {
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select v from ProductVariant v join fetch v.product where v.id = :id") Optional<ProductVariant> findByIdForUpdate(@Param("id") Long id);
}
