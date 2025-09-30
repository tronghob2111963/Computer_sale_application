package com.trong.Computer_sell.DTO.request;


import com.trong.Computer_sell.common.Gender;
import com.trong.Computer_sell.common.UserType;
import jakarta.validation.constraints.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@ToString
public class UserUpdateRequestDTO implements Serializable {

    @NotNull(message = "Id is required")
    private UUID id;
    @NotBlank(message = "First name must be not blank")
    private String firstName;
    @NotBlank(message = "Last name must be not blank")
    private String lastName;
    private Gender gender;
    private LocalDate dateOfBirth;
    private String phoneNumber;
    @Email(message = "Email is not valid")
    private String email;
    private List<AddressRequestDTO> addresses;
}
