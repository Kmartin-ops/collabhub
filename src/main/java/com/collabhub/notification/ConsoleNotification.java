package com.collabhub.notification;

public class ConsoleNotification implements Notifiable {

    @Override
    public void notify(String recipient, String message) {
        System.out.println("[NOTIFICATION → " + recipient + "] "
                + message
                + "  (thread: " + Thread.currentThread().getName() + ")");
    }
}