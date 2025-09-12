package com.muiyurocodes.ecommerc.repository;

import com.muiyurocodes.ecommerc.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    //CRUD method implementation is provided by JpaRepository
}
