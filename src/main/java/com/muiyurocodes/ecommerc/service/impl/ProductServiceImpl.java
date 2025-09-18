package com.muiyurocodes.ecommerc.service.impl;

import com.muiyurocodes.ecommerc.dto.ProductDTO;
import com.muiyurocodes.ecommerc.model.Product;
import com.muiyurocodes.ecommerc.repository.ProductRepository;
import com.muiyurocodes.ecommerc.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    @Override
    public Page<ProductDTO> getAllProducts(Pageable pageable) {
        Page<Product> productPage = productRepository.findAll(pageable);
        return productPage.map(product -> modelMapper.map(product, ProductDTO.class));
    }

    @Override
    public Optional<ProductDTO> getProductById(Long productId) {
        return productRepository.findById(productId)
                .map(product -> modelMapper.map(product, ProductDTO.class));
    }

    @Override
    public Page<ProductDTO> searchProductsByName(String name, Pageable pageable) {
        Page<Product> productPage = productRepository.findByNameContainingIgnoreCase(name, pageable);
        return productPage.map(product -> modelMapper.map(product, ProductDTO.class));
    }

    @Override
    public Page<ProductDTO> getInStockProducts(Pageable pageable) {
        Page<Product> productPage = productRepository.findAllByStockQuantityGreaterThan(0, pageable);
        return productPage.map(product -> modelMapper.map(product, ProductDTO.class));
    }
}
