package com.collabhub.controller;

import com.collabhub.config.CollabHubProperties;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;

@RestController
public class HealthController {

    private final Environment environment;
    private final CollabHubProperties properties;

    public HealthController(Environment environment,
                            CollabHubProperties properties) {
        this.environment = environment;
        this.properties  = properties;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
                "status",    "UP",
                "app",       "CollabHub",
                "timestamp", LocalDateTime.now().toString()
        );
    }

    @GetMapping("/info")
    public Map<String, Object> info() {
        return Map.of(
                "app",         environment.getProperty("spring.application.name"),
                "profiles",    Arrays.toString(environment.getActiveProfiles()),
                "description", properties.getAppDescription(),
                "config",      Map.of(
                        "dispatcherThreads", properties.getNotifications().getDispatcherThreads(),
                        "queueCapacity",     properties.getNotifications().getQueueCapacity(),
                        "defaultPageSize",   properties.getPagination().getDefaultPageSize()
                )
        );
    }
}