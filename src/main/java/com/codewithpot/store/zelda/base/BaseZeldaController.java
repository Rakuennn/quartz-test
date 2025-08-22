package com.codewithpot.store.zelda.base;

import com.codewithpot.store.common.base.BaseController;
import com.codewithpot.store.zelda.dto.ZeldaCharacterResponse;
import org.springframework.http.ResponseEntity;

public abstract class BaseZeldaController extends BaseController {
    public abstract ResponseEntity<ZeldaCharacterResponse> getZeldaCharacter(String name);
}
