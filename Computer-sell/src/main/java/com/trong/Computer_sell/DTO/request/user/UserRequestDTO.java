package com.trong.Computer_sell.DTO.request.user;

import com.trong.Computer_sell.common.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.trong.Computer_sell.common.UserType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;


@Getter
@Setter
public class UserRequestDTO {

    @NotNull(message = "Username is required")
    private String username;
    @NotNull(message = "Password is required")
    private String password;

    @NotBlank(message = "First name must be not blank")
    private String firstName;

    @NotBlank(message = "Last name must be not blank")
    private String lastName;
    private Gender gender;

    private LocalDate dateOfBirth;
    private String phoneNumber;

    @Email(message = "Email is not valid")
    private String email;
    private UserType userType;
    private List<AddressRequestDTO> addresses;
}
