package com.muiyurocodes.ecommerc.repository;

import com.muiyurocodes.ecommerc.model.Order;
import com.muiyurocodes.ecommerc.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    // CRUD method implementation is provided by JpaRepository
    Page<Order> findByUser(User user, Pageable pageable);

    Page<Order> findByStatus(String status, Pageable pageable);

    Optional<Order> findByIdAndUser(Long id, User user);
}
