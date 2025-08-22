package com.codewithpot.store.auth.dto.Request;

import com.codewithpot.store.common.Enum.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class UpdateUserRequest {
    @NotNull
    private UUID id;
    @NotBlank
    private String userName;
    @NotBlank
    private String email;
    @NotNull
    private int age;
    @NotNull
    private boolean active;
    @NotNull
    private LocalDate birthDate;
    private Gender gender = Gender.MALE;
    private BigDecimal accountBalance = BigDecimal.ZERO;
}
