package com.codewithpot.store.json.model;

import lombok.Data;

@Data
public class MainUser {

    private String username;
    private Integer userId;
    private Boolean isActive;
}
