package com.muiyurocodes.ecommerc.repository;

import com.muiyurocodes.ecommerc.model.Cart;
import com.muiyurocodes.ecommerc.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    //CRUD method implementation is provided by JpaRepository
    Optional<Cart> findByUser(User user);
}
