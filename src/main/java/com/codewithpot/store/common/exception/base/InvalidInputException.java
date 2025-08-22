package com.codewithpot.store.common.exception.base;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
public class InvalidInputException extends RuntimeException{
    private String messageProperties;
    private String errorCode;
    private List<String> fieldNameList;

    public InvalidInputException(String code, String messageProperties, List<String> fieldNameList) {
        this.messageProperties = messageProperties;
        this.errorCode = code;
        this.fieldNameList = fieldNameList;
    }

}
