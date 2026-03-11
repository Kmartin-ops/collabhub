package com.collabhub.async;

import com.collabhub.notification.Notifiable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;

public class NotificationWorker implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationWorker.class);

    private static final String SHUTDOWN_EVENT = "SHUTDOWN";
    private static final long PROCESSING_DELAY_MS = 50;

    private final BlockingQueue<NotificationEvent> queue;
    private final Notifiable notifier;
    private final String workerName;

    // Volatile ensures visibility across threads
    private volatile boolean running = true;

    public NotificationWorker(BlockingQueue<NotificationEvent> queue, Notifiable notifier, String workerName) {
        this.queue = queue;
        this.notifier = notifier;
        this.workerName = workerName;
    }

    @Override
    public void run() {
        if (LOG.isInfoEnabled()) {
            LOG.info("[{}] Started on thread: {}", workerName, Thread.currentThread());
        }

        boolean shouldRun = true;

        while (shouldRun && running) {
            NotificationEvent event = null;
            try {
                event = queue.take(); // blocking call, efficient waiting

                if (SHUTDOWN_EVENT.equals(event.eventType())) {
                    if (LOG.isInfoEnabled()) {
                        LOG.info("[{}] Received shutdown signal.", workerName);
                    }
                    shouldRun = false; // only one exit from loop
                } else {
                    processEvent(event);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                if (LOG.isWarnEnabled()) {
                    LOG.warn("[{}] Interrupted — shutting down.", workerName, e);
                }
                shouldRun = false; // exit loop on interruption
            }
        }

        if (LOG.isInfoEnabled()) {
            LOG.info("[{}] Stopped.", workerName);
        }
    }

    private void processEvent(NotificationEvent event) {
        try {
            Thread.sleep(PROCESSING_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            if (LOG.isWarnEnabled()) {
                LOG.warn("[{}] Interrupted during event processing.", workerName, e);
            }
        }

        notifier.notify(event.recipient(), event.message());

        if (LOG.isInfoEnabled()) {
            LOG.info("[{}] Processed event: {} | id={}", workerName, event.eventType(), event.eventId());
        }
    }

    public void stop() {
        running = false;
    }
}