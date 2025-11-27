package com.codewithpot.store.auth.base;

import com.codewithpot.store.auth.dto.Request.CreateUserRequest;
import com.codewithpot.store.auth.dto.Request.UpdateUserRequest;
import com.codewithpot.store.auth.dto.Response.CreateUserResponse;
import com.codewithpot.store.auth.dto.Response.UpdateUserResponse;
import com.codewithpot.store.auth.dto.Response.DeleteUserResponse;
import com.codewithpot.store.common.base.BaseController;
import com.codewithpot.store.json.model.UserModel;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

public abstract class BaseAuthController extends BaseController {
    @Operation(summary = "Get User")
    public abstract ResponseEntity<UserModel> getUser();

    @Operation(summary = "Create User")
    public abstract ResponseEntity<CreateUserResponse> createUser(CreateUserRequest req);

    @Operation(summary = "Update User")
    public abstract ResponseEntity<UpdateUserResponse> updateUser(UpdateUserRequest req);

    @Operation(summary = "Delete User")
    public abstract ResponseEntity<DeleteUserResponse> deleteUser(UUID id);
}