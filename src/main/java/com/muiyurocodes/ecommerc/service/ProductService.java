package com.muiyurocodes.ecommerc.service;

import com.muiyurocodes.ecommerc.dto.CategoryDTO;
import com.muiyurocodes.ecommerc.dto.ProductDTO;
import com.muiyurocodes.ecommerc.dto.ProductResponseDTO;

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
}
