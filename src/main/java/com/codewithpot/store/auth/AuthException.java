package com.codewithpot.store.auth;

import com.codewithpot.store.common.constant.ErrorCodeConstant;
import com.codewithpot.store.common.constant.ResultDescriptionConstant;
import com.codewithpot.store.common.exception.base.AllException;
import com.codewithpot.store.common.exception.base.InvalidInputException;
import org.springframework.http.HttpStatus;

import java.util.Collections;

public class AuthException extends RuntimeException {
    public static InvalidInputException invalidBody(String bodyName) {
        return new InvalidInputException(
                ErrorCodeConstant.INVALID_BODY_PARAMETER_CODE,
                ResultDescriptionConstant.DESCRIPTION_400001,
                Collections.singletonList(bodyName)
        );
    }

    public static AllException userNameNotFound() {
        return new AllException("Username not found", HttpStatus.NOT_FOUND);
    }

    public static AllException userNotFound() {
        return new AllException("User not found",HttpStatus.NOT_FOUND);
    }

    public static AllException ageNotCorrect(){
        return new AllException("Age must be between 12 and 120" ,HttpStatus.BAD_REQUEST);
    }

    public static AllException birthdateNotCorrect(){
        return new AllException("Birthdate not correct",HttpStatus.BAD_REQUEST);
    }

}
