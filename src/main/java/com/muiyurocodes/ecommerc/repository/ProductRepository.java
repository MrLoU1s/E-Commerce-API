package com.muiyurocodes.ecommerc.repository;

import com.muiyurocodes.ecommerc.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    //CRUD method implementation is provided by JpaRepository
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Optional<Product> findByIdAndStockQuantityGreaterThan(Long id, int quantity);

    List<Product> findAllByStockQuantityGreaterThan(int quantity);

    Page<Product> findAllByStockQuantityGreaterThan(int quantity, Pageable pageable);

    List<Product> findByCategoryId(Long categoryId);
}
