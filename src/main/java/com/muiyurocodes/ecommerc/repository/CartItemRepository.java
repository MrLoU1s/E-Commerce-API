package com.muiyurocodes.ecommerc.repository;

import com.muiyurocodes.ecommerc.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    //CRUD method implementation is provided by JpaRepository
}
