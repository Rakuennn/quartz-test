package com.codewithpot.store.zelda;

import com.codewithpot.store.zelda.base.BaseZeldaService;
import com.codewithpot.store.zelda.dto.ZeldaCharacterResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ZeldaService implements BaseZeldaService {
    @Autowired
    private ZeldaClient zeldaClient;

    public ZeldaCharacterResponse getZeldaCharacter(String name){
        ZeldaCharacterResponse res = zeldaClient.getCharacters(name);
        return res;
    }
}
