package com.collabhub.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component // ← Spring manages this, injects it into TaskService
public class ConsoleNotification implements Notifiable {

    private static final Logger LOG = LoggerFactory.getLogger(ConsoleNotification.class);

    @Override
    public void notify(String recipient, String message) {
        if (LOG.isInfoEnabled()) {
            LOG.info("[NOTIFICATION → {}] {}  (thread: {})", recipient, message, Thread.currentThread().getName());
        }
    }
}
