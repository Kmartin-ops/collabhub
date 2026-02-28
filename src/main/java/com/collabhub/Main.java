package com.collabhub;

import com.collabhub.async.NotificationDispatcher;
import com.collabhub.domain.*;
import com.collabhub.notification.ConsoleNotification;
import com.collabhub.report.ReportEngine;
import com.collabhub.service.ProjectService;
import com.collabhub.service.TaskService;

import java.time.LocalDate;

public class Main {

    public static void main(String[] args) throws InterruptedException {

        ConsoleNotification notifier = new ConsoleNotification();
        ProjectService projectService = new ProjectService();
        TaskService taskService = new TaskService(notifier);

        // Start dispatcher — 3 virtual thread workers, queue capacity 50
        NotificationDispatcher dispatcher = new NotificationDispatcher(notifier, 3, 50);
        taskService.setDispatcher(dispatcher);

        // --- Users ---
        User alice = new User("Alice", "alice@collabhub.com", "MANAGER");
        User bob   = new User("Bob",   "bob@collabhub.com",   "DEVELOPER");
        User carol = new User("Carol", "carol@collabhub.com", "DEVELOPER");

        // --- Project ---
        Project mvp = projectService.createProject("CollabHub MVP", "Core platform", alice);
        projectService.addMember(mvp, bob);
        projectService.addMember(mvp, carol);

        // --- Tasks ---
        System.out.println("\n--- Creating & Assigning Tasks ---");
        Task t1 = taskService.createTask("Write tests",     "MEDIUM", LocalDate.now().plusDays(5),  mvp, alice);
        Task t2 = taskService.createTask("Build REST API",  "HIGH",   LocalDate.now().plusDays(7),  mvp, alice);
        Task t3 = taskService.createTask("Set up CI/CD",    "HIGH",   LocalDate.now().plusDays(2),  mvp, alice);
        Task t4 = taskService.createTask("Design database", "MEDIUM", LocalDate.now().plusDays(1),  mvp, alice);
        Task t5 = taskService.createTask("Fix login bug",   "HIGH",   LocalDate.now().minusDays(1), mvp, alice);

        taskService.assignTask(t1, carol, alice);
        taskService.assignTask(t2, bob,   alice);
        taskService.assignTask(t3, bob,   alice);
        taskService.assignTask(t4, carol, alice);
        taskService.assignTask(t5, bob,   alice);

        taskService.changeStatus(t3, "IN_PROGRESS", bob);
        taskService.changeStatus(t4, "DONE", carol);

        // Give workers time to process the queue
        System.out.println("\n--- Main thread continues while workers process ---");
        System.out.println("[Main] Queue size right now: " + dispatcher.getQueueSize());

        // Wait for workers to drain the queue
        Thread.sleep(1000);

        // Report
        ReportEngine report = new ReportEngine(taskService.getRegistry());
        report.printFullReport();

        // Graceful shutdown
        dispatcher.shutdown();
    }
}