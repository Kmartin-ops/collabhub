package com.collabhub;

import com.collabhub.domain.*;
import com.collabhub.notification.ConsoleNotification;
import com.collabhub.report.PagedResult;
import com.collabhub.report.ReportEngine;
import com.collabhub.service.ProjectService;
import com.collabhub.service.TaskService;

import java.time.LocalDate;

public class Main {

    public static void main(String[] args) {

        ConsoleNotification notifier = new ConsoleNotification();
        ProjectService projectService = new ProjectService();
        TaskService taskService = new TaskService(notifier);

        // --- Users ---
        User alice = new User("Alice", "alice@collabhub.com", "MANAGER");
        User bob   = new User("Bob",   "bob@collabhub.com",   "DEVELOPER");
        User carol = new User("Carol", "carol@collabhub.com", "DEVELOPER");

        // --- Project ---
        Project mvp = projectService.createProject("CollabHub MVP", "Core platform", alice);
        projectService.addMember(mvp, bob);
        projectService.addMember(mvp, carol);

        // --- Tasks with varied dates/priorities ---
        Task t1 = taskService.createTask("Write tests",     "MEDIUM", LocalDate.now().plusDays(5),  mvp, alice);
        Task t2 = taskService.createTask("Build REST API",  "HIGH",   LocalDate.now().plusDays(7),  mvp, alice);
        Task t3 = taskService.createTask("Set up CI/CD",    "HIGH",   LocalDate.now().plusDays(2),  mvp, alice);
        Task t4 = taskService.createTask("Design database", "MEDIUM", LocalDate.now().plusDays(1),  mvp, alice);
        Task t5 = taskService.createTask("Fix login bug",   "HIGH",   LocalDate.now().minusDays(1), mvp, alice);
        Task t6 = taskService.createTask("Update docs",     "LOW",    LocalDate.now().plusDays(10), mvp, alice);

        // --- Assign ---
        taskService.assignTask(t1, carol, alice);
        taskService.assignTask(t2, bob,   alice);
        taskService.assignTask(t3, bob,   alice);
        taskService.assignTask(t4, carol, alice);
        taskService.assignTask(t5, bob,   alice);
        taskService.assignTask(t6, carol, alice);

        // --- Some status changes ---
        taskService.changeStatus(t4, "IN_PROGRESS", carol);
        taskService.changeStatus(t3, "IN_PROGRESS", bob);
        taskService.changeStatus(t1, "DONE", carol);

        // --- Wire up ReportEngine ---
        ReportEngine report = new ReportEngine(taskService.getRegistry());

        // --- Full report ---
        report.printFullReport();

        // --- Paged results ---
        System.out.println("📄 Page 0 (3 per page):");
        PagedResult<Task> page0 = report.getTasksPaged(0, 3);
        System.out.println(page0);
        page0.getContent().forEach(t -> System.out.println("   " + t.describe()));

        System.out.println("\n📄 Page 1 (3 per page):");
        PagedResult<Task> page1 = report.getTasksPaged(1, 3);
        System.out.println(page1);
        page1.getContent().forEach(t -> System.out.println("   " + t.describe()));
    }
}