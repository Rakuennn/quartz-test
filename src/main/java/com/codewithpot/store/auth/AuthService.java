package com.codewithpot.store.auth;

import com.codewithpot.store.auth.base.BaseAuthService;
import com.codewithpot.store.auth.dto.Request.DeleteUserRequest;
import com.codewithpot.store.auth.dto.Request.CreateUserRequest;
import com.codewithpot.store.auth.dto.Request.UpdateUserRequest;
import com.codewithpot.store.auth.dto.Response.DeleteUserResponse;
import com.codewithpot.store.auth.dto.Response.GetUserResponse;
import com.codewithpot.store.auth.dto.Response.CreateUserResponse;
import com.codewithpot.store.auth.dto.Response.UpdateUserResponse;
import com.codewithpot.store.common.constant.ResultDescriptionConstant;
import com.codewithpot.store.common.entity.shoply.UserEntity;
import com.codewithpot.store.common.repository.shoply.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
public class AuthService implements BaseAuthService {

    private final UserRepository userRepo;

    public AuthService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public List<GetUserResponse> getUser() {
        List<UserEntity> userList = userRepo.findAll();

        return userList.stream().map(user -> {
            GetUserResponse res = new GetUserResponse();
            res.setUserName(user.getUserName())
                    .setEmail(user.getEmail())
                    .setAge(user.getAge())
                    .setBirthDate(user.getBirthDate())
                    .setGender(user.getGender())
                    .setAccountBalance(user.getAccountBalance())
                    .setActive(user.getActive())
                    .setCreatedAt(user.getCreatedAt())
                    .setUpdatedAt(user.getCreatedAt())
                    .setUpdatedAt(user.getUpdatedAt())
                    .setMessage(ResultDescriptionConstant.DESCRIPTION_200000)
                    .setStatus("Get users successful");
            return res;
        }).toList();
    }

    @Override
    public CreateUserResponse createUser(CreateUserRequest req) {
        if (userRepo.existsByUserName(req.getUserName())) {
            throw AuthException.invalidBody("username");
        }

        if (userRepo.existsByEmail(req.getEmail())) {
            throw AuthException.invalidBody("email");
        }

        if(req.getAge() <= 10 || req.getAge() >= 120){
            throw AuthException.ageNotCorrect();
        }

        int age = Period.between(req.getBirthDate(), LocalDate.now()).getYears();
        if(age < 10){
            throw AuthException.birthdateNotCorrect();
        }

        UserEntity user = new UserEntity();
        user.setUserName(req.getUserName())
                .setPassword(req.getPassword())
                .setEmail(req.getEmail())
                .setAge(req.getAge())
                .setBirthDate(req.getBirthDate())
                .setGender(req.getGender())
                .setAccountBalance(req.getAccountBalance())
                .setActive(true)
                .setCreatedAt(LocalDateTime.now());

        userRepo.save(user);

        CreateUserResponse res = new CreateUserResponse();
        res.setStatus(ResultDescriptionConstant.DESCRIPTION_200000);
        res.setMessage("User created successfully");
        return res;
    }

    @Override
    public UpdateUserResponse updateUser(UpdateUserRequest req){
        UserEntity user = userRepo.findNameById(req.getId());
        if(user == null){
            throw AuthException.userNotFound();
        }

        if (userRepo.existsByUserNameAndUserIdNot(req.getUserName(), req.getId())) {
            throw AuthException.invalidBody("username");
        }

        if (userRepo.existsByEmailAndUserIdNot(req.getEmail(), req.getId())) {
            throw AuthException.invalidBody("email");
        }

        if(req.getAge() <= 10 || req.getAge() >= 120){
            throw AuthException.ageNotCorrect();
        }

        int age = Period.between(req.getBirthDate(), LocalDate.now()).getYears();
        if(age < 12){
            throw AuthException.birthdateNotCorrect();
        }

        user.setUserName(req.getUserName())
                .setEmail(req.getEmail())
                .setAge(req.getAge())
                .setActive(req.isActive())
                .setBirthDate(req.getBirthDate())
                .setGender(req.getGender())
                .setAccountBalance(req.getAccountBalance())
                .setUpdatedAt(LocalDateTime.now());
        userRepo.save(user);

        UpdateUserResponse res = new UpdateUserResponse();
        res.setUserName(req.getUserName())
                .setEmail(req.getEmail())
                .setAge(req.getAge())
                .setActive(req.isActive())
                .setBirthDate(req.getBirthDate())
                .setGender(req.getGender())
                .setUpdatedAt(LocalDateTime.now())
                .setAccountBalance(req.getAccountBalance())
                .setStatus(ResultDescriptionConstant.DESCRIPTION_200000)
                .setMessage("User changed successfully");
        return res;
    }

    @Override
    public DeleteUserResponse deleteUser(UUID id){
        Optional<UserEntity> user = userRepo.findById(id);
        if (user.isEmpty()) {
            throw AuthException.userNotFound();
        }

        userRepo.deleteUserById(id);

        DeleteUserResponse res = new DeleteUserResponse();
        res.setStatus(ResultDescriptionConstant.DESCRIPTION_200000);
        res.setMessage("User delete successfully");
        return res;
    }
}

