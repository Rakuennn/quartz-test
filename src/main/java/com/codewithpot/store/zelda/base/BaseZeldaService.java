package com.codewithpot.store.zelda.base;

import com.codewithpot.store.zelda.dto.ZeldaCharacterResponse;

public interface BaseZeldaService {
    ZeldaCharacterResponse getZeldaCharacter(String name);
}
