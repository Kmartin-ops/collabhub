package com.collabhub;

import com.collabhub.domain.*;
import com.collabhub.notification.ConsoleNotification;
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
        projectService.addMember(mvp, bob); // duplicate — Set rejects this silently

        // --- Tasks with varied due dates ---
        System.out.println("\n--- Creating Tasks ---");
        Task t1 = taskService.createTask("Write tests",       "MEDIUM",
                LocalDate.now().plusDays(5),  mvp, alice);
        Task t2 = taskService.createTask("Build REST API",    "HIGH",
                LocalDate.now().plusDays(7),  mvp, alice);
        Task t3 = taskService.createTask("Set up CI/CD",      "HIGH",
                LocalDate.now().plusDays(2),  mvp, alice);
        Task t4 = taskService.createTask("Design database",   "MEDIUM",
                LocalDate.now().plusDays(1),  mvp, alice);
        Task t5 = taskService.createTask("Fix login bug",     "HIGH",
                LocalDate.now().minusDays(1), mvp, alice); // already overdue!

        // --- Assign ---
        System.out.println("\n--- Assigning Tasks ---");
        taskService.assignTask(t1, carol, alice);
        taskService.assignTask(t2, bob,   alice);
        taskService.assignTask(t3, bob,   alice);
        taskService.assignTask(t4, carol, alice);
        taskService.assignTask(t5, bob,   alice);

        // --- Priority queue in action ---
        System.out.println("\n--- Tasks by Due Date (PriorityQueue order) ---");
        taskService.findAllSortedByDueDate()
                .forEach(t -> System.out.println("  " + t.getDueDate() + " | " + t.getTitle()));

        // --- Next urgent task ---
        System.out.println("\n--- Most Urgent Task ---");
        taskService.getNextUrgentTask()
                .ifPresent(t -> System.out.println("  → " + t.describe()));

        // --- Overdue tasks ---
        System.out.println("\n--- Overdue Tasks ---");
        taskService.findOverdue()
                .forEach(t -> System.out.println("  ⚠️  " + t.describe()));

        // --- Bob's workload ---
        System.out.println("\n--- Bob's Tasks ---");
        taskService.findByAssignee(bob)
                .forEach(t -> System.out.println("  " + t.describe()));

        // --- Status change ---
        System.out.println("\n--- Status Changes ---");
        taskService.changeStatus(t4, "IN_PROGRESS", carol);
        taskService.changeStatus(t4, "DONE", carol);

        // --- Projects Alice is on ---
        System.out.println("\n--- Alice's Projects ---");
        projectService.findByMember(alice)
                .forEach(System.out::println);
    }
}