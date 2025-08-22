package com.codewithpot.store.common.Enum;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public enum Gender {
    MALE("Male"),
    FEMALE("FEMALE"),
    OTHER("OTHER ๆ");

    private final String description;

    Gender(String description) {
        this.description = description;
    }

}
