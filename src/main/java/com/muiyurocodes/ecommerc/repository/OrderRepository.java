package com.muiyurocodes.ecommerc.repository;

import com.muiyurocodes.ecommerc.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    //CRUD method implementation is provided by JpaRepository
}
