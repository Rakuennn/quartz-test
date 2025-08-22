package com.codewithpot.store.common.exception.base;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class AllException extends RuntimeException {
    private HttpStatus status;

    public AllException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
