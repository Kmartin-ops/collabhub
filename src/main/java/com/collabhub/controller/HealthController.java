package com.collabhub.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController  // @Controller + @ResponseBody — all methods return JSON
public class HealthController {

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
                "status",    "UP",
                "app",       "CollabHub",
                "timestamp", LocalDateTime.now().toString()
        );
    }
}