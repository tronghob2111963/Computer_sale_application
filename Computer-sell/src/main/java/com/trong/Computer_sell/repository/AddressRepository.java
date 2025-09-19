package com.trong.Computer_sell.repository;

import com.trong.Computer_sell.common.AddressType;
import com.trong.Computer_sell.model.AddressEntity;
import org.springframework.boot.autoconfigure.amqp.RabbitConnectionDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<AddressEntity, Long> {

    AddressEntity findByUserIdAndAddressType(Long userId, AddressType addressType);

}
