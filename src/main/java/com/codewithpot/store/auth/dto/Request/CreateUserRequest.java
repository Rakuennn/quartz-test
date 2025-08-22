package com.codewithpot.store.auth.dto.Request;

import com.codewithpot.store.common.Enum.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;



@Data
public class CreateUserRequest {
    @NotBlank(message = "email is required")
    private String email;

    @NotBlank(message = "userName must not be blank")
    private String userName;

    @NotBlank(message = "password is required")
    private String password;

    @NotNull(message = "age is required")
    private int age;

    @NotNull(message = "birthday  is required")
    private LocalDate birthDate;

    @Schema(description = "Gender of the user", example = "MALE")
    private Gender gender = Gender.MALE;

    private BigDecimal accountBalance = BigDecimal.ZERO;
}
