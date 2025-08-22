package com.codewithpot.store.auth.dto.Response;

import com.codewithpot.store.common.Enum.Gender;
import com.codewithpot.store.common.base.BaseResponse;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;


import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
public class GetUserResponse extends BaseResponse {
    private UUID userId;
    private String userName;
    private String email;
    private int age;
    private boolean isActive;
    private LocalDate birthDate;
    private Gender gender;
    private BigDecimal accountBalance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
