package com.codewithpot.store.auth;

import com.codewithpot.store.auth.base.BaseAuthController;
import com.codewithpot.store.auth.dto.Request.CreateUserRequest;
import com.codewithpot.store.auth.dto.Request.UpdateUserRequest;
import com.codewithpot.store.auth.dto.Response.CreateUserResponse;
import com.codewithpot.store.auth.dto.Response.UpdateUserResponse;
import com.codewithpot.store.auth.dto.Response.DeleteUserResponse;
import com.codewithpot.store.json.model.UserModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@RestController
@Slf4j
@Tag(name = "Auth")
public class AuthController extends BaseAuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("auth/get-user")
    @Override
    public ResponseEntity<UserModel> getUser() {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            ClassPathResource resource = new ClassPathResource("json/data.json");
            InputStream inputStream = resource.getInputStream();

            UserModel userModel = objectMapper.readValue(inputStream, UserModel.class);

            log.info("------------------------------------------");
            log.info("DATA FROM JSON: " + userModel);
            log.info("------------------------------------------");

            return ResponseEntity.ok(userModel);

        } catch (IOException e) {
            log.error("Error reading JSON file", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("auth/create-user")
    @Override
    public ResponseEntity<CreateUserResponse> createUser(
            @Valid @RequestBody CreateUserRequest req
    ) {
        return ResponseEntity.ok(authService.createUser(req));
    }

    @PutMapping("auth/update-user")
    @Override
    public ResponseEntity<UpdateUserResponse> updateUser(
            @Valid @RequestBody UpdateUserRequest req
    ){
        return ResponseEntity.ok(authService.updateUser(req));
    }

    @DeleteMapping("auth/delete-user/{id}")
    @Override
    public ResponseEntity<DeleteUserResponse> deleteUser(
            @PathVariable("id") UUID id
    ){
        return ResponseEntity.ok(authService.deleteUser(id));
    }
}