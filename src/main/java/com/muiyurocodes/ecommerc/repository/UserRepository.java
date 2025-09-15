package com.muiyurocodes.ecommerc.repository;


import com.muiyurocodes.ecommerc.model.Role;
import com.muiyurocodes.ecommerc.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    //CRUD method implementation is provided by JpaRepository
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRole(Role role);

}
