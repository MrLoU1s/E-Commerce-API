package com.muiyurocodes.ecommerc.service.impl;

import com.muiyurocodes.ecommerc.dto.*;
import com.muiyurocodes.ecommerc.exception.CategoryNotFoundException;
import com.muiyurocodes.ecommerc.exception.ProductNotFoundException;
import com.muiyurocodes.ecommerc.model.Category;
import com.muiyurocodes.ecommerc.model.Product;
import com.muiyurocodes.ecommerc.repository.CategoryRepository;
import com.muiyurocodes.ecommerc.repository.ProductRepository;
import com.muiyurocodes.ecommerc.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    @Override
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        Category category = modelMapper.map(categoryDTO, Category.class);
        Category savedCategory = categoryRepository.save(category);
        return modelMapper.map(savedCategory, CategoryDTO.class);
    }

    @Override
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(category -> modelMapper.map(category, CategoryDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDTO getCategoryById(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + categoryId));
        return modelMapper.map(category, CategoryDTO.class);
    }

    @Override
    public void deleteCategory(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new CategoryNotFoundException("Category not found with id: " + categoryId);
        }
        // Note: This logic might need adjustment based on desired behavior for products
        // in a deleted category.
        // Here, we are disassociating products from the category.
        productRepository.findByCategoryId(categoryId).forEach(product -> {
            product.setCategory(null);
            productRepository.save(product);
        });
        categoryRepository.deleteById(categoryId);
    }

    @Override
    public ProductResponseDTO createProduct(ProductDTO productDTO) {
        Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException(
                        "Category not found with id: " + productDTO.getCategoryId()));

        Product product = modelMapper.map(productDTO, Product.class);
        product.setCategory(category);

        Product savedProduct = productRepository.save(product);
        return modelMapper.map(savedProduct, ProductResponseDTO.class);
    }

    @Override
    public ProductResponseDTO getProductById(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));
        return modelMapper.map(product, ProductResponseDTO.class);
    }

    @Override
    public List<ProductResponseDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(product -> modelMapper.map(product, ProductResponseDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponseDTO> getProductsByCategory(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new CategoryNotFoundException("Category not found with id: " + categoryId);
        }
        return productRepository.findByCategoryId(categoryId).stream()
                .map(product -> modelMapper.map(product, ProductResponseDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public ProductResponseDTO updateProduct(Long productId, ProductDTO productDTO) {
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));

        Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException(
                        "Category not found with id: " + productDTO.getCategoryId()));

        // Use ModelMapper to map fields from DTO to existing entity, preserving the ID
        modelMapper.map(productDTO, existingProduct);
        existingProduct.setCategory(category); // Re-associate the category

        Product updatedProduct = productRepository.save(existingProduct);
        return modelMapper.map(updatedProduct, ProductResponseDTO.class);
    }

    @Override
    public void deleteProduct(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException("Product not found with id: " + productId);
        }
        productRepository.deleteById(productId);
    }

    @Override
    public Page<ProductResponseDTO> searchProducts(
            String name,
            String description,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Long categoryId,
            Boolean inStock,
            Pageable pageable) {

        // Use the repository method to search products with the given criteria
        Page<Product> productPage = productRepository.searchProducts(
                name, description, minPrice, maxPrice, categoryId, inStock, pageable);

        // Map the products to DTOs
        return productPage.map(product -> modelMapper.map(product, ProductResponseDTO.class));
    }

    @Override
    public List<ProductResponseDTO> getLowStockProducts(int threshold) {
        // Find products with stock quantity less than or equal to the threshold
        List<Product> lowStockProducts = productRepository.findAllByStockQuantityLessThanEqual(threshold);

        // Map the products to DTOs
        return lowStockProducts.stream()
                .map(product -> modelMapper.map(product, ProductResponseDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<ProductResponseDTO> updateProductStock(List<ProductStockUpdateDTO> stockUpdates) {
        List<Product> updatedProducts = stockUpdates.stream()
                .map(update -> {
                    // Find the product by ID
                    Product product = productRepository.findById(update.getProductId())
                            .orElseThrow(() -> new ProductNotFoundException(
                                    "Product not found with id: " + update.getProductId()));

                    // Update the stock quantity
                    product.setStockQuantity(update.getStockQuantity());

                    // Save the updated product
                    return productRepository.save(product);
                })
                .collect(Collectors.toList());

        // Map the updated products to DTOs
        return updatedProducts.stream()
                .map(product -> modelMapper.map(product, ProductResponseDTO.class))
                .collect(Collectors.toList());
    }
}