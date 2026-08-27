package com.ecommerce.project.repositories;

import com.ecommerce.project.models.Product;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long> {
    List<Product> findByCategoryId(Long categoryId);

    @Modifying
    @Query("""
            update Product p
            set p.quantity = p.quantity - :quantity,
                p.version = p.version + 1
            where p.id = :productId and p.quantity >= :quantity
            """)
    int decreaseQuantityIfAvailable(@Param("productId") Long productId,
                                    @Param("quantity") Long quantity);
}
