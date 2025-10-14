package com.muiyurocodes.ecommerc.repository;

import com.muiyurocodes.ecommerc.model.Address;
import com.muiyurocodes.ecommerc.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    
    List<Address> findByUser(User user);
    
    List<Address> findByUserAndAddressType(User user, String addressType);
    
    Optional<Address> findByUserAndIsDefaultAndAddressType(User user, Boolean isDefault, String addressType);
}