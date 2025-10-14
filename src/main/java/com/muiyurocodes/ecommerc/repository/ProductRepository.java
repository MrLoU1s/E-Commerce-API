package com.muiyurocodes.ecommerc.repository;

import com.muiyurocodes.ecommerc.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // CRUD method implementation is provided by JpaRepository

    // Search by name
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // Search by description
    Page<Product> findByDescriptionContainingIgnoreCase(String description, Pageable pageable);

    // Advanced search with multiple criteria
    @Query("SELECT p FROM Product p WHERE " +
            "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:description IS NULL OR LOWER(p.description) LIKE LOWER(CONCAT('%', :description, '%'))) AND " +
            "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
            "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
            "(:inStock IS NULL OR (:inStock = true AND p.stockQuantity > 0) OR (:inStock = false))")
    Page<Product> searchProducts(
            @Param("name") String name,
            @Param("description") String description,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("categoryId") Long categoryId,
            @Param("inStock") Boolean inStock,
            Pageable pageable);

    // Find product by ID with stock check
    Optional<Product> findByIdAndStockQuantityGreaterThan(Long id, int quantity);

    // Find all products with stock greater than specified quantity
    List<Product> findAllByStockQuantityGreaterThan(int quantity);

    // Find all products with stock greater than specified quantity (paginated)
    Page<Product> findAllByStockQuantityGreaterThan(int quantity, Pageable pageable);

    // Find all products with stock less than or equal to specified threshold (for
    // low stock alerts)
    List<Product> findAllByStockQuantityLessThanEqual(int threshold);

    // Find products by category
    List<Product> findByCategoryId(Long categoryId);
}