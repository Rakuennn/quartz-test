package com.codewithpot.store.auth.dto.Response;

import com.codewithpot.store.common.Enum.Gender;
import com.codewithpot.store.common.base.BaseResponse;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Accessors(chain = true)
@Data
public class UpdateUserResponse extends BaseResponse {
    private String userName;
    private String email;
    private int age;
    private boolean active;
    private LocalDate birthDate;
    private Gender gender;
    private BigDecimal accountBalance;
    private LocalDateTime updatedAt;
}
