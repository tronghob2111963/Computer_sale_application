package com.trong.Computer_sell.DTO.response.User;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailResponseDTO implements Serializable {
    private UUID id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String gender;
    private LocalDate dateOfBirth;
    private String phoneNumber;
    private String userType;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<AddressResponseDTO> addresses;
    private List<String> roles;
}
