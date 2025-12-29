package com.trong.Computer_sell.controller;

import com.trong.Computer_sell.DTO.request.address.AddressRequestDTO;
import com.trong.Computer_sell.DTO.response.address.AddressResponseDTO;
import com.trong.Computer_sell.DTO.response.common.ResponseData;
import com.trong.Computer_sell.DTO.response.common.ResponseError;
import com.trong.Computer_sell.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j(topic = "ADDRESS_CONTROLLER")
@RestController
@RequestMapping("/address")
@Tag(name = "Address Management", description = "APIs for managing user addresses")
@RequiredArgsConstructor
@Validated
public class AddressController {

    private final AddressService addressService;

    @Operation(summary = "Get all addresses by user ID", description = "Retrieve all addresses for a specific user")
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyAuthority('SysAdmin','Admin', 'Staff', 'User')")
    public ResponseData<List<AddressResponseDTO>> getAddressesByUserId(@PathVariable UUID userId) {
        try {
            log.info("Getting addresses for user: {}", userId);
            List<AddressResponseDTO> addresses = addressService.getAddressesByUserId(userId);
            return new ResponseData<>(HttpStatus.OK.value(), "Addresses retrieved successfully", addresses);
        } catch (Exception e) {
            log.error("Error getting addresses: {}", e.getMessage());
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @Operation(summary = "Get address by ID", description = "Retrieve a specific address by its ID")
    @GetMapping("/{addressId}")
    @PreAuthorize("hasAnyAuthority('SysAdmin','Admin', 'Staff', 'User')")
    public ResponseData<AddressResponseDTO> getAddressById(@PathVariable UUID addressId) {
        try {
            log.info("Getting address by id: {}", addressId);
            AddressResponseDTO address = addressService.getAddressById(addressId);
            return new ResponseData<>(HttpStatus.OK.value(), "Address retrieved successfully", address);
        } catch (Exception e) {
            log.error("Error getting address: {}", e.getMessage());
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @Operation(summary = "Create new address", description = "Create a new address for a user")
    @PostMapping("/user/{userId}")
    @PreAuthorize("hasAnyAuthority('SysAdmin','Admin', 'Staff', 'User')")
    public ResponseData<AddressResponseDTO> createAddress(
            @PathVariable UUID userId,
            @RequestBody @Valid AddressRequestDTO request) {
        try {
            log.info("Creating address for user: {}", userId);
            AddressResponseDTO created = addressService.createAddress(userId, request);
            return new ResponseData<>(HttpStatus.CREATED.value(), "Address created successfully", created);
        } catch (Exception e) {
            log.error("Error creating address: {}", e.getMessage());
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @Operation(summary = "Update address", description = "Update an existing address")
    @PutMapping("/user/{userId}/{addressId}")
    @PreAuthorize("hasAnyAuthority('SysAdmin','Admin', 'Staff', 'User')")
    public ResponseData<AddressResponseDTO> updateAddress(
            @PathVariable UUID userId,
            @PathVariable UUID addressId,
            @RequestBody @Valid AddressRequestDTO request) {
        try {
            log.info("Updating address {} for user: {}", addressId, userId);
            AddressResponseDTO updated = addressService.updateAddress(userId, addressId, request);
            return new ResponseData<>(HttpStatus.OK.value(), "Address updated successfully", updated);
        } catch (Exception e) {
            log.error("Error updating address: {}", e.getMessage());
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @Operation(summary = "Delete address", description = "Delete an address by ID")
    @DeleteMapping("/user/{userId}/{addressId}")
    @PreAuthorize("hasAnyAuthority('SysAdmin','Admin', 'Staff', 'User')")
    public ResponseData<Void> deleteAddress(
            @PathVariable UUID userId,
            @PathVariable UUID addressId) {
        try {
            log.info("Deleting address {} for user: {}", addressId, userId);
            addressService.deleteAddress(userId, addressId);
            return new ResponseData<>(HttpStatus.OK.value(), "Address deleted successfully", null);
        } catch (Exception e) {
            log.error("Error deleting address: {}", e.getMessage());
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }
}
