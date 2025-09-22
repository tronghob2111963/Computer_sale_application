package com.trong.Computer_sell.controller;


import com.trong.Computer_sell.DTO.request.UserCreationRequestDTO;
import com.trong.Computer_sell.DTO.request.UserPasswordRequest;
import com.trong.Computer_sell.DTO.request.UserRequestDTO;
import com.trong.Computer_sell.DTO.request.UserUpdateRequestDTO;
import com.trong.Computer_sell.DTO.response.ResponseData;
import com.trong.Computer_sell.DTO.response.ResponseError;
import com.trong.Computer_sell.DTO.response.UserResponseDTO;
import com.trong.Computer_sell.exception.ErrorResponse;
import com.trong.Computer_sell.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j(topic = "USER_CONTROLLER")
@RestController
@RequestMapping("/user")
@Tag(name = "User Management")
@RequiredArgsConstructor
@Validated
public class UserController {


    private final UserService userService;


    @Operation(summary = "list user" , description = "List of User")
    @GetMapping("/list")
    @PreAuthorize("hasAnyAuthority('SysAdmin','Staff','Admin')")
    public ResponseData<?> findAllUser(
            @RequestParam(required = false) String keyword ,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sortBy
    ){
        try{
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("data", userService.findAll(keyword, page, size, sortBy));
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
    @PreAuthorize("hasAnyAuthority('SysAdmin','Admin', 'Staff')")
    public ResponseData<Object> finndUserById(@PathVariable Long id){
        log.info("Find user with user id", id);
        try{
            log.info("Find user with user id", id);
            return new ResponseData<>(HttpStatus.OK.value(), "User found successfully", userService.findById(id));
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
    @PreAuthorize("hasAuthority('SysAdmin')")
    public ResponseData<Object> saveUser(@RequestBody UserRequestDTO user){
        try {
            log.info("Save user with user name", user);
            userService.saveUser(user);
            return new ResponseData<>(HttpStatus.ACCEPTED.value(), "User saved successfully");
        } catch (Exception e) {
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }
}
