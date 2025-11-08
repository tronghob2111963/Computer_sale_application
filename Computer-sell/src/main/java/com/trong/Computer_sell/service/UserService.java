package com.trong.Computer_sell.service;

import com.trong.Computer_sell.DTO.request.user.UserCreationRequestDTO;
import com.trong.Computer_sell.DTO.request.user.UserPasswordRequest;
import com.trong.Computer_sell.DTO.request.user.UserRequestDTO;
import com.trong.Computer_sell.DTO.request.user.UserUpdateRequestDTO;
import com.trong.Computer_sell.DTO.response.common.PageResponse;
import com.trong.Computer_sell.DTO.response.User.UserResponseDTO;

import java.util.UUID;

public interface UserService {

    PageResponse<?> findAll(String keyword, int pageNo, int pageSize, String sortBy);
    UserResponseDTO findById(UUID id);
    UserResponseDTO findByUsername(String username);
    UserResponseDTO findByEmail(String email);
    UUID RegisterUser(UserCreationRequestDTO req);
    void update(UserUpdateRequestDTO req);
    void delete(UUID id);
    void changePassword(UserPasswordRequest req);
    UUID saveUser(UserRequestDTO req);
    PageResponse<?> findAllCustomerUser(String keyword, int pageNo, int pageSize, String sortBy);


}
