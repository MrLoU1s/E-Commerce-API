package com.muiyurocodes.ecommerc.repository;

import com.muiyurocodes.ecommerc.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    //CRUD method implementation is provided by JpaRepository
}
