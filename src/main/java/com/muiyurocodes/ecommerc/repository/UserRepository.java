package com.muiyurocodes.ecommerc.repository;


import com.muiyurocodes.ecommerc.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    //CRUD method implementation is provided by JpaRepository

}
