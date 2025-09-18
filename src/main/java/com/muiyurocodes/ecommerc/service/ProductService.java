package com.muiyurocodes.ecommerc.service;

import com.muiyurocodes.ecommerc.dto.ProductDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface ProductService {
    Page<ProductDTO> getAllProducts(Pageable pageable);

    Optional<ProductDTO> getProductById(Long productId);

    Page<ProductDTO> searchProductsByName(String name, Pageable pageable);

    Page<ProductDTO> getInStockProducts(Pageable pageable);
}
