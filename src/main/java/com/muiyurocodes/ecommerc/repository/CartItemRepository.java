package com.muiyurocodes.ecommerc.repository;

import com.muiyurocodes.ecommerc.model.Cart;
import com.muiyurocodes.ecommerc.model.CartItem;
import com.muiyurocodes.ecommerc.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    //CRUD method implementation is provided by JpaRepository
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);

    List<CartItem> findByCart(Cart cart);

    void deleteAllByCart(Cart cart);
}
