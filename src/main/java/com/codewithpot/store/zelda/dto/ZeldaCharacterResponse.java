package com.codewithpot.store.zelda.dto;

import lombok.Data;

import java.util.List;

@Data
public class ZeldaCharacterResponse {
    private boolean success;
    private int count;
    private List<ZeldaCharacter> data;
}
