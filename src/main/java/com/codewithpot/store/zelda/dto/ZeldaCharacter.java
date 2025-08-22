package com.codewithpot.store.zelda.dto;

import lombok.Data;

import java.util.List;

@Data
public class ZeldaCharacter {
    private String id;
    private String name;
    private String description;
    private String gender;
    private String race;
    private List<String> appearances;
}