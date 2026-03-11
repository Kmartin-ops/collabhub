package com.collabhub.async;

import com.collabhub.notification.Notifiable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class NotificationDispatcher {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationDispatcher.class);

    private final BlockingQueue<NotificationEvent> queue;
    private final ExecutorService executor;
    private final List<NotificationWorker> workers;
    private final int workerCount;

    public NotificationDispatcher(Notifiable notifier, int workerCount, int queueCapacity) {
        this.workerCount = workerCount;
        this.queue = new LinkedBlockingQueue<>(queueCapacity);
        this.workers = new ArrayList<>();

        // Virtual thread executor — one virtual thread per submitted task
        this.executor = Executors.newVirtualThreadPerTaskExecutor();

        // Start the workers
        for (int i = 1; i <= workerCount; i++) {
            NotificationWorker worker = new NotificationWorker(queue, notifier, "Worker-" + i);
            workers.add(worker);
            executor.execute(worker);
        }

        if (LOG.isInfoEnabled()) {
            LOG.info("[Dispatcher] Started with {} virtual thread workers. Queue capacity: {}", workerCount, queueCapacity);
        }
    }

    // Non-blocking dispatch — puts event on queue and returns immediately
    public void dispatch(NotificationEvent event) {
        try {
            boolean accepted = queue.offer(event, 100, TimeUnit.MILLISECONDS);
            if (!accepted && LOG.isWarnEnabled()) {
                LOG.warn("[Dispatcher] ⚠️  Queue full — dropped event: {} for {}", event.eventType(), event.recipient());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            if (LOG.isErrorEnabled()) {
                LOG.error("[Dispatcher] Interrupted while dispatching event: {} for {}", event.eventType(), event.recipient(), e);
            }
        }
    }

    // Graceful shutdown — drain the queue first, then stop workers
    public void shutdown() {
        if (LOG.isInfoEnabled()) {
            LOG.info("[Dispatcher] Shutting down — draining queue ({} remaining)...", queue.size());
        }

        // Send one poison pill per worker
        for (int i = 0; i < workerCount; i++) {
            try {
                queue.put(NotificationEvent.of("system", "shutdown", "SHUTDOWN"));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                if (LOG.isErrorEnabled()) {
                    LOG.error("[Dispatcher] Interrupted while sending shutdown signal to workers", e);
                }
            }
        }

        executor.shutdown();

        try {
            // Wait up to 5 seconds for all workers to finish
            boolean finished = executor.awaitTermination(5, TimeUnit.SECONDS);
            if (finished) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("[Dispatcher] All workers stopped cleanly.");
                }
            } else {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("[Dispatcher] Timeout — forcing shutdown.");
                }
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
            if (LOG.isErrorEnabled()) {
                LOG.error("[Dispatcher] Interrupted while awaiting termination", e);
            }
        }
    }

    public int getQueueSize() {
        return queue.size();
    }
}