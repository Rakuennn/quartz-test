package com.codewithpot.store.json;

import com.codewithpot.store.json.model.UserModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;

import java.io.IOException;

@Component
public class ConfigLoader {

    @Value("classpath:json/data.json")
    private Resource resource;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() throws IOException {
        UserModel user = objectMapper.readValue(resource.getInputStream(), UserModel.class);
        System.out.println("User loaded: " + user);
    }
}
