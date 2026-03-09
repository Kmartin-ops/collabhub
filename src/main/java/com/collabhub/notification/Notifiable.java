package com.collabhub.notification;

public interface Notifiable {

    // Every notification type must implement this
    void notify(String recipient, String message);
}
