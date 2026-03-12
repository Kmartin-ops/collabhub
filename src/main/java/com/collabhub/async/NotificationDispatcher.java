package com.collabhub.async;

import com.collabhub.notification.Notifiable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class NotificationDispatcher {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationDispatcher.class);
    private static final String SHUTDOWN_EVENT = "SHUTDOWN";

    private final BlockingQueue<NotificationEvent> queue;
    private final ExecutorService executor;
    private final int workerCount;

    public NotificationDispatcher(Notifiable notifier, int workerCount, int queueCapacity) {
        this.workerCount = workerCount;
        this.queue = new LinkedBlockingQueue<>(queueCapacity);

        // Virtual thread executor
        this.executor = Executors.newVirtualThreadPerTaskExecutor();

        startWorkers(notifier);

        if (LOG.isInfoEnabled()) {
            LOG.info("[Dispatcher] Started with {} virtual thread workers. Queue capacity: {}", workerCount, queueCapacity);
        }
    }

    private void startWorkers(Notifiable notifier) {
        for (int i = 1; i <= workerCount; i++) {
            NotificationWorker worker = new NotificationWorker(queue, notifier, "Worker-" + i);
            executor.execute(worker);
        }
    }

    // Non-blocking dispatch — puts event on queue and returns immediately
    public void dispatch(NotificationEvent event) {
        try {
            boolean accepted = queue.offer(event, 100, TimeUnit.MILLISECONDS);

            if (!accepted) {
                logQueueFull(event);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logDispatchInterrupted(event, e);
        }
    }

    // Graceful shutdown — drain the queue first, then stop workers
    public void shutdown() {
        logShutdownStart();
        sendShutdownSignals();
        stopExecutor();
    }

    private void logShutdownStart() {
        if (LOG.isInfoEnabled()) {
            LOG.info("[Dispatcher] Shutting down — draining queue ({} remaining)...", queue.size());
        }
    }

    private void sendShutdownSignals() {
        for (int i = 0; i < workerCount; i++) {
            try {
                queue.put(NotificationEvent.of("system", "shutdown", SHUTDOWN_EVENT));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logShutdownSignalError(e);
            }
        }
    }

    private void stopExecutor() {
        executor.shutdown();

        try {
            boolean finished = executor.awaitTermination(5, TimeUnit.SECONDS);

            if (finished) {
                logCleanShutdown();
            } else {
                logForcedShutdown();
                executor.shutdownNow();
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
            logAwaitTerminationError(e);
        }
    }

    private void logQueueFull(NotificationEvent event) {
        if (LOG.isWarnEnabled()) {
            LOG.warn("[Dispatcher] ⚠️ Queue full — dropped event: {} for {}", event.eventType(), event.recipient());
        }
    }

    private void logDispatchInterrupted(NotificationEvent event, InterruptedException e) {
        if (LOG.isErrorEnabled()) {
            LOG.error("[Dispatcher] Interrupted while dispatching event: {} for {}", event.eventType(), event.recipient(), e);
        }
    }

    private void logShutdownSignalError(InterruptedException e) {
        if (LOG.isErrorEnabled()) {
            LOG.error("[Dispatcher] Interrupted while sending shutdown signal to workers", e);
        }
    }

    private void logAwaitTerminationError(InterruptedException e) {
        if (LOG.isErrorEnabled()) {
            LOG.error("[Dispatcher] Interrupted while awaiting termination", e);
        }
    }

    private void logCleanShutdown() {
        if (LOG.isInfoEnabled()) {
            LOG.info("[Dispatcher] All workers stopped cleanly.");
        }
    }

    private void logForcedShutdown() {
        if (LOG.isWarnEnabled()) {
            LOG.warn("[Dispatcher] Timeout — forcing shutdown.");
        }
    }

    public int getQueueSize() {
        return queue.size();
    }
}