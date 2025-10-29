package com.chattingbox.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class DebugController {

    @Value("${spring.data.mongodb.uri:}")
    private String mongoUri;

    @Value("${spring.security.oauth2.client.registration.google.client-id:}")
    private String googleClientId;

    @GetMapping("/internal/config")
    public Map<String, Object> config() {
        return Map.of(
                "mongoUriPresent", mongoUri != null && !mongoUri.isBlank(),
                "googleClientIdPresent", googleClientId != null && !googleClientId.isBlank());
    }
}
