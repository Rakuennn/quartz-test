package com.codewithpot.store.auth.dto.Request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoginRequest {
    private String userName;
    private String password;
}
