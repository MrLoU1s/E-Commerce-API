package com.muiyurocodes.ecommerc.service.impl;

import com.muiyurocodes.ecommerc.dto.CartDTO;
import com.muiyurocodes.ecommerc.dto.CartItemDTO;
import com.muiyurocodes.ecommerc.exception.InsufficientStockException;
import com.muiyurocodes.ecommerc.exception.ProductNotFoundException;
import com.muiyurocodes.ecommerc.exception.UserNotFoundException;
import com.muiyurocodes.ecommerc.model.*;
import com.muiyurocodes.ecommerc.repository.*;
import com.muiyurocodes.ecommerc.service.CartService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final ModelMapper modelMapper;

    @Override
    public CartDTO getCartForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> createCartForUser(user));
        return convertToCartDTO(cart);
    }

    @Override
    public CartDTO addProductToCart(Long userId, Long productId, int quantity) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found for id: " + productId));

        if (product.getStockQuantity() < quantity) {
            throw new InsufficientStockException("Not enough stock for product with id: " + productId);
        }

        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> createCartForUser(user));

        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product)
                .orElse(new CartItem());

        if (cartItem.getId() == null) { // New item
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItem.setPrice(product.getPrice()); // **FIX: Lock in the price**
            cart.getItems().add(cartItem);
        } else { // Existing item
            int newQuantity = cartItem.getQuantity() + quantity;
            if (product.getStockQuantity() < newQuantity) {
                throw new InsufficientStockException("Not enough stock for product with id: " + productId);
            }
            cartItem.setQuantity(newQuantity);
        }

        cartItemRepository.save(cartItem);
        return convertToCartDTO(cart);
    }

    @Override
    public CartDTO updateCartItem(Long userId, Long productId, int quantity) {
        if (quantity <= 0) {
            return removeProductFromCart(userId, productId);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new IllegalStateException("User does not have a cart"));

        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product)
                .orElseThrow(() -> new ProductNotFoundException("Product not in cart"));

        if (product.getStockQuantity() < quantity) {
            throw new InsufficientStockException("Not enough stock for product with id: " + productId);
        }

        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);

        return convertToCartDTO(cart);
    }

    @Override
    public CartDTO removeProductFromCart(Long userId, Long productId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new IllegalStateException("User does not have a cart"));

        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product)
                .orElseThrow(() -> new ProductNotFoundException("Product not in cart"));

        cart.getItems().remove(cartItem);
        cartItemRepository.delete(cartItem);

        return convertToCartDTO(cart);
    }

    @Override
    public void clearCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        cartRepository.findByUser(user).ifPresent(cart -> {
            cartItemRepository.deleteAllByCart(cart);
        });
    }

    private Cart createCartForUser(User user) {
        Cart newCart = new Cart();
        newCart.setUser(user);
        return cartRepository.save(newCart);
    }

    private CartDTO convertToCartDTO(Cart cart) {
        CartDTO cartDTO = new CartDTO();
        cartDTO.setId(cart.getId());

        List<CartItemDTO> itemDTOs = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;

        // **FIX: Single, efficient loop for conversion and calculation**
        for (CartItem item : cart.getItems()) {
            CartItemDTO itemDTO = convertToCartItemDTO(item);
            itemDTOs.add(itemDTO);
            if (itemDTO.getPrice() != null) {
                totalPrice = totalPrice.add(itemDTO.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity())));
            }
        }

        cartDTO.setItems(itemDTOs);
        cartDTO.setTotalPrice(totalPrice);

        return cartDTO;
    }

    private CartItemDTO convertToCartItemDTO(CartItem cartItem) {
        CartItemDTO cartItemDTO = modelMapper.map(cartItem, CartItemDTO.class);
        cartItemDTO.setProductId(cartItem.getProduct().getId());
        cartItemDTO.setProductName(cartItem.getProduct().getName());

        // **FIX: Use locked-in price with fallback for backward compatibility**
        if (cartItem.getPrice() != null) {
            cartItemDTO.setPrice(cartItem.getPrice());
        } else {
            // Fallback for old data where price wasn't saved on CartItem
            cartItemDTO.setPrice(cartItem.getProduct().getPrice());
        }

        return cartItemDTO;
    }
}
