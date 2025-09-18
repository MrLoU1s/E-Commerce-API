package com.muiyurocodes.ecommerc.controller;

import com.muiyurocodes.ecommerc.dto.ProductDTO;
import com.muiyurocodes.ecommerc.exception.ProductNotFoundException;
import com.muiyurocodes.ecommerc.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<Page<ProductDTO>> getAllProducts(Pageable pageable) {
        return ResponseEntity.ok(productService.getAllProducts(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id)));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductDTO>> searchProducts(@RequestParam("q") String query, Pageable pageable) {
        return ResponseEntity.ok(productService.searchProductsByName(query, pageable));
    }

    @GetMapping("/in-stock")
    public ResponseEntity<Page<ProductDTO>> getInStockProducts(Pageable pageable) {
        return ResponseEntity.ok(productService.getInStockProducts(pageable));
    }
}
