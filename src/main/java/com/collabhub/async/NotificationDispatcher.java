package com.collabhub.async;

import com.collabhub.notification.Notifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class NotificationDispatcher {

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
            executor.submit(worker); // each worker runs on its own virtual thread
        }

        System.out.println("[Dispatcher] Started with " + workerCount
                + " virtual thread workers. Queue capacity: " + queueCapacity);
    }

    // Non-blocking dispatch — puts event on queue and returns immediately
    public void dispatch(NotificationEvent event) {
        try {
            boolean accepted = queue.offer(event, 100, TimeUnit.MILLISECONDS);
            if (!accepted) {
                System.err.println("[Dispatcher] ⚠️  Queue full — dropped event: "
                        + event.eventType() + " for " + event.recipient());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Graceful shutdown — drain the queue first, then stop workers
    public void shutdown() {
        System.out.println("[Dispatcher] Shutting down — draining queue ("
                + queue.size() + " remaining)...");

        // Send one poison pill per worker
        for (int i = 0; i < workerCount; i++) {
            try {
                queue.put(NotificationEvent.of("system", "shutdown", "SHUTDOWN"));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        executor.shutdown();

        try {
            // Wait up to 5 seconds for all workers to finish
            boolean finished = executor.awaitTermination(5, TimeUnit.SECONDS);
            if (finished) {
                System.out.println("[Dispatcher] All workers stopped cleanly.");
            } else {
                System.out.println("[Dispatcher] Timeout — forcing shutdown.");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
    }

    public int getQueueSize() {
        return queue.size();
    }
}