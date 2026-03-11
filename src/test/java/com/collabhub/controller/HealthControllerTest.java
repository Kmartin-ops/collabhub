package com.collabhub.controller;

import com.collabhub.config.CollabHubProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("HealthController")
class HealthControllerTest {

    @Mock
    private Environment environment;

    private HealthController healthController;

    @BeforeEach
    void setUp() {
        CollabHubProperties properties = new CollabHubProperties();
        properties.setAppDescription("Team collaboration backend");

        CollabHubProperties.Notifications notifications = new CollabHubProperties.Notifications();
        notifications.setDispatcherThreads(4);
        notifications.setQueueCapacity(200);
        properties.setNotifications(notifications);

        CollabHubProperties.Pagination pagination = new CollabHubProperties.Pagination();
        pagination.setDefaultPageSize(20);
        pagination.setMaxPageSize(100);
        properties.setPagination(pagination);

        healthController = new HealthController(environment, properties);
    }

    @Test
    @DisplayName("health endpoint should expose basic status payload")
    void shouldReturnHealthPayload() {
        Map<String, Object> payload = healthController.health();

        assertThat(payload)
                .containsEntry("status", "UP")
                .containsEntry("app", "CollabHub");

        assertThat(payload.get("timestamp")).isInstanceOf(String.class);
    }

    @Test
    @DisplayName("info endpoint should expose configured app details")
    void shouldReturnInfoPayload() {
        when(environment.getProperty("spring.application.name")).thenReturn("collabhub");
        when(environment.getActiveProfiles()).thenReturn(new String[]{"dev", "test"});

        Map<String, Object> payload = healthController.info();

        assertThat(payload)
                .containsEntry("app", "collabhub")
                .containsEntry("description", "Team collaboration backend");

        assertThat(payload.get("profiles").toString()).contains("dev");

        @SuppressWarnings("unchecked")
        Map<String, Object> config = (Map<String, Object>) payload.get("config");

        assertThat(config)
                .containsEntry("dispatcherThreads", 4)
                .containsEntry("queueCapacity", 200)
                .containsEntry("defaultPageSize", 20);
    }
}