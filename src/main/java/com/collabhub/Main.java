package com.collabhub;

import com.collabhub.async.NotificationDispatcher;
import com.collabhub.notification.ConsoleNotification;
import com.collabhub.report.ReportEngine;
import com.collabhub.seed.DataSeeder;
import com.collabhub.service.ProjectService;
import com.collabhub.service.TaskService;

public class Main {

    public static void main(String[] args) throws InterruptedException {

        // ── Wire up the app ───────────────────────────────
        ConsoleNotification notifier  = new ConsoleNotification();
        ProjectService projectService = new ProjectService();
        TaskService taskService       = new TaskService(notifier);

        NotificationDispatcher dispatcher =
                new NotificationDispatcher(notifier, 3, 200);
        taskService.setDispatcher(dispatcher);

        // ── Seed data ─────────────────────────────────────
        DataSeeder seeder = new DataSeeder(projectService, taskService);
        seeder.seed();

        // ── Let workers process the notification queue ────
        Thread.sleep(2000);

        // ── Full report ───────────────────────────────────
        ReportEngine report = new ReportEngine(taskService.getRegistry());
        report.printFullReport();

        // ── Shutdown ──────────────────────────────────────
        dispatcher.shutdown();
    }
}