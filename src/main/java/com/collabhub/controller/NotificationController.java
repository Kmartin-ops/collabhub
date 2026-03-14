package com.collabhub.controller;

import com.collabhub.dto.NotificationPayload;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@Tag(name = "WebSocket Notifications")
public class NotificationController {

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void broadcastToProject(String projectId, NotificationPayload payload) {
        messagingTemplate.convertAndSend("/topic/notifications/" + projectId, payload);
    }

    public void sendToUser(String userEmail, NotificationPayload payload) {
        messagingTemplate.convertAndSendToUser(userEmail, "/queue/notifications", payload);
    }

    @MessageMapping("/ping")
    @SendTo("/topic/pong")
    public String ping(String message) {
        return "pong";
    }
}
