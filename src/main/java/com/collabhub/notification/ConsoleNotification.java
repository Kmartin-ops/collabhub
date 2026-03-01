package com.collabhub.notification;

import org.springframework.stereotype.Component;

@Component  // ← Spring manages this, injects it into TaskService
public class ConsoleNotification implements Notifiable {

    @Override
    public void notify(String recipient, String message) {
        System.out.println("[NOTIFICATION → " + recipient + "] "
                + message
                + "  (thread: " + Thread.currentThread().getName() + ")");
    }
}