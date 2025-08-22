package com.codewithpot.store.auth.dto.Request;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class DeleteUserRequest {
    private UUID id;
}
