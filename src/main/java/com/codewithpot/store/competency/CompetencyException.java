package com.codewithpot.store.competency;

import com.codewithpot.store.common.exception.base.AllException;
import org.springframework.http.HttpStatus;

public class CompetencyException extends RuntimeException {

    static public AllException dataNotFound() {
        return new AllException("Data not found", HttpStatus.NOT_FOUND);
    }
}
