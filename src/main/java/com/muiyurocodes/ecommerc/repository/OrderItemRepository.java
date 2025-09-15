package com.muiyurocodes.ecommerc.repository;

import com.muiyurocodes.ecommerc.model.Order;
import com.muiyurocodes.ecommerc.model.OrderItem;
import com.muiyurocodes.ecommerc.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    //CRUD method implementation is provided by JpaRepository
    List<OrderItem> findByOrder(Order order);

    Optional<OrderItem> findByOrderAndProduct(Order order, Product product);
}
