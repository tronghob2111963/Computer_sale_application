package com.trong.Computer_sell.controller;


import com.trong.Computer_sell.DTO.request.user.UserCreationRequestDTO;
import com.trong.Computer_sell.DTO.request.user.UserPasswordRequest;
import com.trong.Computer_sell.DTO.request.user.UserRequestDTO;
import com.trong.Computer_sell.DTO.request.user.UserUpdateRequestDTO;
import com.trong.Computer_sell.DTO.response.common.ResponseData;
import com.trong.Computer_sell.DTO.response.common.ResponseError;
import com.trong.Computer_sell.common.AddressType;
import com.trong.Computer_sell.model.AddressEntity;
import com.trong.Computer_sell.model.UserEntity;
import com.trong.Computer_sell.repository.AddressRepository;
import com.trong.Computer_sell.repository.UserRepository;
import com.trong.Computer_sell.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

@Slf4j(topic = "USER_CONTROLLER")
@RestController
@RequestMapping("/user")
@Tag(name = "User Management")
@RequiredArgsConstructor
@Validated
public class UserController {


    private final UserService userService;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;


    @Operation(summary = "list user" , description = "List of User")
    @GetMapping("/list")
    @PreAuthorize("hasAnyAuthority('SysAdmin','Staff','Admin')")
    public ResponseData<?> findAllUser(
            @RequestParam(required = false) String keyword ,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) Integer roleId
    ){
        try{
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("data", userService.findAll(keyword, page, size, sortBy, roleId));
            return new ResponseData<>(HttpStatus.OK.value(), "Find all user successfully", result);
        } catch (Exception e) {
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @Operation(summary = "Create User")
    @PostMapping("/register")
    public ResponseData<Object> ResigterUser(@RequestBody @Valid UserCreationRequestDTO user){
        log.info("Create User with user name", user.getUsername() );
        try {
            log.info("Create User with user name", user.getUsername() );
            return new ResponseData<>(HttpStatus.CREATED.value(), "User created successfully", userService.RegisterUser(user));
        } catch (Exception e) {
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @Operation(summary = "update user" , description = "Update user by id")
    @PostMapping("/update")
    @PreAuthorize("hasAnyAuthority('SysAdmin','User')")
    public ResponseData<Object> updateUser(@RequestBody UserUpdateRequestDTO user){
        log.info("Update User with user name", user);
        try{
            log.info("Update User with user name", user);
            userService.update(user);
            return new ResponseData<>(HttpStatus.ACCEPTED.value(), "User updated successfully");
        }catch (Exception e){
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @Operation(summary = "Change password", description = "Change password by user id")
    @PatchMapping("/chang-pwd")
    @PreAuthorize("hasAnyAuthority('USER')")
    public ResponseData<Object> changePassword(@RequestBody UserPasswordRequest user){
        try {
            log.info("Change password with user name", user);

            userService.changePassword(user);
            return new ResponseData<>(HttpStatus.ACCEPTED.value(), "Password changed successfully");

        } catch (Exception e) {
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }

    }


    @Operation(summary = "delete user" , description = "Delete user by id")
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('SysAdmin')")
    public ResponseData<Object> deleteUser(@PathVariable @Min(value = 1 , message = "Id must be greater than 0") Long id){
        try {
            log.info("Delete user with user id", id);
            return new ResponseData<>(HttpStatus.ACCEPTED.value(), "User deleted successfully");
        } catch (Exception e) {
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @Operation(summary = "find by id" , description = "Find user by id")
    @GetMapping("/find/{id}")
    @PreAuthorize("hasAnyAuthority('SysAdmin','Admin', 'Staff', 'User')")
    public ResponseData<Object> finndUserById(@PathVariable UUID id){
        log.info("Find user with user id", id);
        try{
            log.info("Find user with user id", id);
            return new ResponseData<>(HttpStatus.OK.value(), "User found successfully", userService.findById(id));
        } catch (RuntimeException e) {
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @Operation(summary = "find detail by id" , description = "Find user detail by id including addresses")
    @GetMapping("/detail/{id}")
    @PreAuthorize("hasAnyAuthority('SysAdmin','Admin', 'Staff', 'User')")
    public ResponseData<Object> findUserDetailById(@PathVariable UUID id){
        log.info("Find user detail with user id: {}", id);
        try{
            return new ResponseData<>(HttpStatus.OK.value(), "User detail found successfully", userService.findDetailById(id));
        } catch (RuntimeException e) {
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }


    @Operation(summary = "comfirm email" , description = "Comfirm email by user id")
    @GetMapping("/comfirm-email")
    @PreAuthorize("hasAnyAuthority('USER')")
    public void confirmEmail(@RequestParam String secretCode, HttpServletResponse response) throws IOException {
        try {
            log.info("Confirm email with secret code", secretCode);

        } catch (Exception e) {
            log.error("Confirm email with secret code", e.getMessage());
            throw new RuntimeException(e);
        }
        //TODO Finally -> trang chu
    }

    @Operation(summary = "create staff and admin user", description = "Create staff and admin user")
    @PostMapping("/save")
//    @PreAuthorize("hasAuthority('SysAdmin')")
    public ResponseData<Object> saveUser(@RequestBody UserRequestDTO user){
        try {
            log.info("Save user with user name", user);

            return new ResponseData<>(HttpStatus.ACCEPTED.value(), "User saved successfully",userService.saveUser(user));
        } catch (Exception e) {
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    // ================================
    // ADDRESS MANAGEMENT APIs
    // ================================

    @Operation(summary = "Add address", description = "Add new address for user")
    @PostMapping("/{userId}/address")
    @PreAuthorize("hasAnyAuthority('SysAdmin','Admin', 'Staff', 'User')")
    public ResponseData<Object> addAddress(@PathVariable UUID userId, @RequestBody Map<String, String> addressData) {
        try {
            log.info("Adding address for user: {}", userId);
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            AddressEntity address = new AddressEntity();
            address.setUser(user);
            address.setApartmentNumber(addressData.get("apartmentNumber"));
            address.setStreetNumber(addressData.get("streetNumber"));
            address.setWard(addressData.get("ward"));
            address.setCity(addressData.get("city"));
            address.setAddressType(AddressType.valueOf(addressData.getOrDefault("addressType", "HOME")));

            AddressEntity saved = addressRepository.save(address);
            log.info("Address added successfully for user: {}", userId);

            Map<String, Object> result = new HashMap<>();
            result.put("id", saved.getId());
            result.put("apartmentNumber", saved.getApartmentNumber());
            result.put("streetNumber", saved.getStreetNumber());
            result.put("ward", saved.getWard());
            result.put("city", saved.getCity());
            result.put("addressType", saved.getAddressType().name());

            return new ResponseData<>(HttpStatus.CREATED.value(), "Address added successfully", result);
        } catch (Exception e) {
            log.error("Error adding address: {}", e.getMessage());
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @Operation(summary = "Update address", description = "Update existing address")
    @PutMapping("/{userId}/address/{addressId}")
    @PreAuthorize("hasAnyAuthority('SysAdmin','Admin', 'Staff', 'User')")
    public ResponseData<Object> updateAddress(@PathVariable UUID userId, @PathVariable UUID addressId, @RequestBody Map<String, String> addressData) {
        try {
            log.info("Updating address {} for user: {}", addressId, userId);
            AddressEntity address = addressRepository.findById(addressId)
                    .orElseThrow(() -> new RuntimeException("Address not found"));

            if (!address.getUser().getId().equals(userId)) {
                throw new RuntimeException("Address does not belong to this user");
            }

            if (addressData.containsKey("apartmentNumber")) {
                address.setApartmentNumber(addressData.get("apartmentNumber"));
            }
            if (addressData.containsKey("streetNumber")) {
                address.setStreetNumber(addressData.get("streetNumber"));
            }
            if (addressData.containsKey("ward")) {
                address.setWard(addressData.get("ward"));
            }
            if (addressData.containsKey("city")) {
                address.setCity(addressData.get("city"));
            }
            if (addressData.containsKey("addressType")) {
                address.setAddressType(AddressType.valueOf(addressData.get("addressType")));
            }

            addressRepository.save(address);
            log.info("Address updated successfully");

            return new ResponseData<>(HttpStatus.OK.value(), "Address updated successfully");
        } catch (Exception e) {
            log.error("Error updating address: {}", e.getMessage());
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @Operation(summary = "Delete address", description = "Delete address by id")
    @DeleteMapping("/{userId}/address/{addressId}")
    @PreAuthorize("hasAnyAuthority('SysAdmin','Admin', 'Staff', 'User')")
    public ResponseData<Object> deleteAddress(@PathVariable UUID userId, @PathVariable UUID addressId) {
        try {
            log.info("Deleting address {} for user: {}", addressId, userId);
            AddressEntity address = addressRepository.findById(addressId)
                    .orElseThrow(() -> new RuntimeException("Address not found"));

            if (!address.getUser().getId().equals(userId)) {
                throw new RuntimeException("Address does not belong to this user");
            }

            addressRepository.delete(address);
            log.info("Address deleted successfully");

            return new ResponseData<>(HttpStatus.OK.value(), "Address deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting address: {}", e.getMessage());
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @Operation(summary = "Get user addresses", description = "Get all addresses of a user")
    @GetMapping("/{userId}/addresses")
    @PreAuthorize("hasAnyAuthority('SysAdmin','Admin', 'Staff', 'User')")
    public ResponseData<Object> getUserAddresses(@PathVariable UUID userId) {
        try {
            log.info("Getting addresses for user: {}", userId);
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<Map<String, Object>> addresses = user.getAddresses().stream().map(addr -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", addr.getId());
                map.put("apartmentNumber", addr.getApartmentNumber());
                map.put("streetNumber", addr.getStreetNumber());
                map.put("ward", addr.getWard());
                map.put("city", addr.getCity());
                map.put("addressType", addr.getAddressType() != null ? addr.getAddressType().name() : "HOME");
                return map;
            }).toList();

            return new ResponseData<>(HttpStatus.OK.value(), "Addresses retrieved successfully", addresses);
        } catch (Exception e) {
            log.error("Error getting addresses: {}", e.getMessage());
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }
}
