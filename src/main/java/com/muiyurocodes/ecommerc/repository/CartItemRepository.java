package com.muiyurocodes.ecommerc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public class CartItemRepository extends JpaRepository<CartItems, Long> {
}
