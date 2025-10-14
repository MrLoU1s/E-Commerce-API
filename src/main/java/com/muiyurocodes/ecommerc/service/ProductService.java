package com.muiyurocodes.ecommerc.service;

import com.muiyurocodes.ecommerc.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {

    // Category methods
    CategoryDTO createCategory(CategoryDTO categoryDTO);

    List<CategoryDTO> getAllCategories();

    CategoryDTO getCategoryById(Long categoryId);

    void deleteCategory(Long categoryId);

    // Product methods
    ProductResponseDTO createProduct(ProductDTO productDTO);

    ProductResponseDTO getProductById(Long productId);

    List<ProductResponseDTO> getAllProducts();

    List<ProductResponseDTO> getProductsByCategory(Long categoryId);

    ProductResponseDTO updateProduct(Long productId, ProductDTO productDTO);

    void deleteProduct(Long productId);

    // Search methods
    Page<ProductResponseDTO> searchProducts(
            String name,
            String description,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Long categoryId,
            Boolean inStock,
            Pageable pageable);

    // Inventory management methods
    List<ProductResponseDTO> getLowStockProducts(int threshold);

    List<ProductResponseDTO> updateProductStock(List<ProductStockUpdateDTO> stockUpdates);
}