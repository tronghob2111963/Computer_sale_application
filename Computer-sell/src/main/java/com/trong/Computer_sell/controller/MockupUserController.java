package com.trong.Computer_sell.controller;

import com.trong.Computer_sell.DTO.response.UserResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/mockup/user")
public class MockupUserController {


//    @Operation(summary = "List all users")
//    @GetMapping("/list")
//    public Map<String, Object> listUser(
//            @RequestParam(required = false) String keyWord,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "20") int size
//
//    ){
//        UserResponseDTO user = new UserResponseDTO();
//        user.setId("1");
//        user.setUsername("user");
//        user.setEmail("user@gmail.com");
//        user.setPassword("123456");
//        user.setFirstName("user");
//        user.setLastName("user");
//        user.setGender("male");
//        user.setDateOfBirth("2000-01-01");
//        user.setPhoneNumber("0123456789");
//
//        UserResponseDTO user1 = new UserResponseDTO();
//        user1.setId("2");
//        user1.setUsername("user1");
//        user1.setEmail("user1@gmail.com");
//        user1.setPassword("123456");
//        user1.setFirstName("user1");
//        user1.setLastName("user1");
//        user1.setGender("male");
//        user1.setDateOfBirth("2000-01-01");
//        user1.setPhoneNumber("0123456789");
//
//        List<UserResponseDTO> users = List.of(user, user1);
//        Map<String, Object> result = new LinkedHashMap<>();
//        result.put("status", HttpStatus.OK.value());
//        result.put("message", "List users successfully");
//        result.put("data", users);
//
//        return result;
//    }
}
