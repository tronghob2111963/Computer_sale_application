package com.trong.Computer_sell.repository;

import com.trong.Computer_sell.common.AddressType;
import com.trong.Computer_sell.model.AddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AddressRepository extends JpaRepository<AddressEntity, UUID> {

    AddressEntity findByUserIdAndAddressType(UUID userId, AddressType addressType);

}
