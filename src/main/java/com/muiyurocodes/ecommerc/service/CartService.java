package com.muiyurocodes.ecommerc.service;

import com.muiyurocodes.ecommerc.dto.CartDTO;

import org.springframework.stereotype.Service;

@Service
public interface CartService {
    CartDTO getCartForUser(Long userId);

    CartDTO addProductToCart(Long userId, Long productId, int quantity);

    CartDTO updateCartItem(Long userId, Long productId, int quantity);

    CartDTO removeProductFromCart(Long userId, Long productId);

    void clearCart(Long userId);
}
