package com.muiyurocodes.ecommerc.service.impl;

import com.muiyurocodes.ecommerc.dto.CartDTO;
import com.muiyurocodes.ecommerc.dto.CartItemDTO;
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
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional // ensures that the ACID properties of a transaction are observed
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
        Product product = productRepository.findByIdAndStockQuantityGreaterThan(productId, quantity - 1)
                .orElseThrow(() -> new ProductNotFoundException("Product not found or not enough stock for id: " + productId));

        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> createCartForUser(user));

        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product)
                .orElse(new CartItem());

        if (cartItem.getId() == null) { // New item
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cart.getItems().add(cartItem);
        } else { // Existing item
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
        }

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

        cartItemRepository.delete(cartItem);
        cart.getItems().remove(cartItem);

        return convertToCartDTO(cart);
    }

    @Override
    public void clearCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        cartRepository.findByUser(user).ifPresent(cartItemRepository::deleteAllByCart);
    }

    private Cart createCartForUser(User user) {
        Cart newCart = new Cart();
        newCart.setUser(user);
        return cartRepository.save(newCart);
    }

    // 1. Input: The 'cart' parameter is the Cart entity object.
    private CartDTO convertToCartDTO(Cart cart) {
        // 2. Initial Mapping: Copies properties like 'id' from the Cart entity to a new CartDTO object.
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

        // 3. Convert Item List: Iterates through each CartItem entity in the cart's item list
        //    and calls 'convertToCartItemDTO' for each one, collecting the results into a list of DTOs.
        List<CartItemDTO> itemDTOs = cart.getItems().stream()
                .map(this::convertToCartItemDTO)
                .collect(Collectors.toList());

        // 5. Finalize DTO (Part 1): Sets the newly created list of CartItemDTOs onto the main CartDTO.
        cartDTO.setItems(itemDTOs);

        // 4. Calculate Total Price: Streams through the list of DTOs, multiplies price by quantity for each,
        //    and sums them up to get the final total.
        // 5. Finalize DTO (Part 2): Sets the calculated total price on the CartDTO.
        cartDTO.setTotalPrice(itemDTOs.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        // 6. Output: Returns the fully constructed CartDTO.
        return cartDTO;
    }

    private CartItemDTO convertToCartItemDTO(CartItem cartItem) {
        CartItemDTO cartItemDTO = modelMapper.map(cartItem, CartItemDTO.class);
        cartItemDTO.setProductId(cartItem.getProduct().getId());
        cartItemDTO.setProductName(cartItem.getProduct().getName());
        cartItemDTO.setPrice(cartItem.getProduct().getPrice());
        return cartItemDTO;
    }
}
