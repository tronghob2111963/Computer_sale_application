package com.trong.Computer_sell.service;

import com.trong.Computer_sell.DTO.request.address.AddressRequestDTO;
import com.trong.Computer_sell.DTO.response.address.AddressResponseDTO;
import com.trong.Computer_sell.common.AddressType;
import com.trong.Computer_sell.exception.ResourceNotFoundException;
import com.trong.Computer_sell.model.AddressEntity;
import com.trong.Computer_sell.model.UserEntity;
import com.trong.Computer_sell.repository.AddressRepository;
import com.trong.Computer_sell.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public List<AddressResponseDTO> getAddressesByUserId(UUID userId) {
        log.info("Getting addresses for user: {}", userId);
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        return user.getAddresses().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public AddressResponseDTO getAddressById(UUID addressId) {
        log.info("Getting address by id: {}", addressId);
        AddressEntity address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));
        return toResponseDTO(address);
    }

    @Transactional
    public AddressResponseDTO createAddress(UUID userId, AddressRequestDTO request) {
        log.info("Creating address for user: {}", userId);
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        AddressEntity address = new AddressEntity();
        address.setUser(user);
        address.setApartmentNumber(request.getApartmentNumber());
        address.setStreetNumber(request.getStreetNumber());
        address.setWard(request.getWard());
        address.setCity(request.getCity());
        address.setAddressType(parseAddressType(request.getAddressType()));

        AddressEntity saved = addressRepository.save(address);
        log.info("Address created successfully with id: {}", saved.getId());
        
        return toResponseDTO(saved);
    }

    @Transactional
    public AddressResponseDTO updateAddress(UUID userId, UUID addressId, AddressRequestDTO request) {
        log.info("Updating address {} for user: {}", addressId, userId);
        
        AddressEntity address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));

        if (!address.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Address does not belong to this user");
        }

        if (request.getApartmentNumber() != null) {
            address.setApartmentNumber(request.getApartmentNumber());
        }
        if (request.getStreetNumber() != null) {
            address.setStreetNumber(request.getStreetNumber());
        }
        if (request.getWard() != null) {
            address.setWard(request.getWard());
        }
        if (request.getCity() != null) {
            address.setCity(request.getCity());
        }
        if (request.getAddressType() != null) {
            address.setAddressType(parseAddressType(request.getAddressType()));
        }

        AddressEntity saved = addressRepository.save(address);
        log.info("Address updated successfully");
        
        return toResponseDTO(saved);
    }

    @Transactional
    public void deleteAddress(UUID userId, UUID addressId) {
        log.info("Deleting address {} for user: {}", addressId, userId);
        
        AddressEntity address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));

        if (!address.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Address does not belong to this user");
        }

        addressRepository.delete(address);
        log.info("Address deleted successfully");
    }

    private AddressResponseDTO toResponseDTO(AddressEntity entity) {
        String fullAddress = String.format("%s, %s, %s, %s",
                entity.getApartmentNumber() != null ? entity.getApartmentNumber() : "",
                entity.getStreetNumber() != null ? entity.getStreetNumber() : "",
                entity.getWard() != null ? entity.getWard() : "",
                entity.getCity() != null ? entity.getCity() : ""
        );

        return AddressResponseDTO.builder()
                .id(entity.getId())
                .apartmentNumber(entity.getApartmentNumber())
                .streetNumber(entity.getStreetNumber())
                .ward(entity.getWard())
                .city(entity.getCity())
                .addressType(entity.getAddressType() != null ? entity.getAddressType().name() : "HOME")
                .fullAddress(fullAddress)
                .build();
    }

    private AddressType parseAddressType(String type) {
        try {
            return AddressType.valueOf(type.toUpperCase());
        } catch (Exception e) {
            return AddressType.HOME;
        }
    }
}
