package com.codewithpot.store.zelda;

import com.codewithpot.store.zelda.base.BaseZeldaController;
import com.codewithpot.store.zelda.dto.ZeldaCharacterResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Zelda")
public class ZeldaController extends BaseZeldaController {

    @Autowired
    private ZeldaService zeldaService;

    //https://zelda.fanapis.com/api/characters?
    @PostMapping("zelda/get-zelda-character")
    @Override
    public ResponseEntity<ZeldaCharacterResponse> getZeldaCharacter(
            @RequestParam(value = "name",required = false) String name
    ){
        return ResponseEntity.ok(zeldaService.getZeldaCharacter(name));
    }
}
