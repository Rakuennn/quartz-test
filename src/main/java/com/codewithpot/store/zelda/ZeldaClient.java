package com.codewithpot.store.zelda;

import com.codewithpot.store.zelda.dto.ZeldaCharacterResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "zelda-api",url = "https://zelda.fanapis.com")
public interface ZeldaClient {
    @GetMapping("api/characters")
    ZeldaCharacterResponse getCharacters(@RequestParam(name ="name", required = false ) String name);
}