package com.codewithpot.store.auth.base;

import com.codewithpot.store.auth.dto.Request.DeleteUserRequest;
import com.codewithpot.store.auth.dto.Request.CreateUserRequest;
import com.codewithpot.store.auth.dto.Request.UpdateUserRequest;
import com.codewithpot.store.auth.dto.Response.DeleteUserResponse;
import com.codewithpot.store.auth.dto.Response.GetUserResponse;
import com.codewithpot.store.auth.dto.Response.CreateUserResponse;
import com.codewithpot.store.auth.dto.Response.UpdateUserResponse;

import java.util.List;
import java.util.UUID;

public interface BaseAuthService {
    List<GetUserResponse> getUser();
    CreateUserResponse createUser(CreateUserRequest req);
    UpdateUserResponse updateUser(UpdateUserRequest req);
    DeleteUserResponse deleteUser(UUID id);

}
