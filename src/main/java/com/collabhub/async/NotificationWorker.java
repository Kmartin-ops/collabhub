package com.collabhub.async;

import com.collabhub.notification.Notifiable;

import java.util.concurrent.BlockingQueue;

public class NotificationWorker implements Runnable {

    private final BlockingQueue<NotificationEvent> queue;
    private final Notifiable notifier;
    private final String workerName;

    // volatile — ensures changes to this flag are visible across threads
    private volatile boolean running = true;

    public NotificationWorker(BlockingQueue<NotificationEvent> queue, Notifiable notifier, String workerName) {
        this.queue = queue;
        this.notifier = notifier;
        this.workerName = workerName;
    }

    @Override
    public void run() {
        System.out.println("[" + workerName + "] Started on thread: " + Thread.currentThread());

        while (running) {
            try {
                // take() blocks here until an event arrives — doesn't burn CPU waiting
                NotificationEvent event = queue.take();

                // Check for the poison pill shutdown signal
                if ("SHUTDOWN".equals(event.eventType())) {
                    System.out.println("[" + workerName + "] Received shutdown signal.");
                    break;
                }

                processEvent(event);

            } catch (InterruptedException e) {
                // Thread was interrupted — restore interrupt flag and exit cleanly
                Thread.currentThread().interrupt();
                System.out.println("[" + workerName + "] Interrupted — shutting down.");
                break;
            }
        }

        System.out.println("[" + workerName + "] Stopped.");
    }

    private void processEvent(NotificationEvent event) {
        // Simulate a small processing delay (like a real network call)
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        notifier.notify(event.recipient(), event.message());
        System.out.println("[" + workerName + "] Processed event: " + event.eventType() + " | id=" + event.eventId());
    }

    public void stop() {
        running = false;
    }
}
