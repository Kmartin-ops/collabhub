package com.collabhub.report;

import com.collabhub.domain.Task;
import com.collabhub.domain.User;
import com.collabhub.registry.TaskRegistry;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class ReportEngine {

    private final TaskRegistry taskRegistry;

    public ReportEngine(TaskRegistry taskRegistry) {
        this.taskRegistry = taskRegistry;
    }

    // ─────────────────────────────────────────────
    // 1. Task summary grouped by assignee
    // ─────────────────────────────────────────────
    public List<TaskSummary> generateAssigneeSummary() {
        LocalDate today = LocalDate.now();

        // Group all tasks by assignee name
        Map<String, List<Task>> byAssignee = taskRegistry.findAll().stream()
                .filter(t -> t.getAssignee() != null) // skip unassigned tasks
                .collect(Collectors.groupingBy(t -> t.getAssignee().getName()));

        // For each assignee, build a TaskSummary record
        return byAssignee.entrySet().stream()
                .map(entry -> {
                    String name = entry.getKey();
                    List<Task> assigneeTasks = entry.getValue();

                    long overdue = assigneeTasks.stream()
                            .filter(t -> t.getDueDate().isBefore(today))
                            .filter(t -> !"DONE".equals(t.getStatus()))
                            .count();

                    long done = assigneeTasks.stream()
                            .filter(t -> "DONE".equals(t.getStatus()))
                            .count();

                    long inProgress = assigneeTasks.stream()
                            .filter(t -> "IN_PROGRESS".equals(t.getStatus()))
                            .count();

                    return new TaskSummary(name, assigneeTasks.size(), overdue, done, inProgress);
                })
                .sorted(Comparator.comparing(TaskSummary::totalTasks).reversed())
                .toList();
    }

    // ─────────────────────────────────────────────
    // 2. Partition tasks into overdue vs on-track
    // ─────────────────────────────────────────────
    public Map<Boolean, List<Task>> partitionByOverdue() {
        LocalDate today = LocalDate.now();

        // partitioningBy always produces exactly two keys: true and false
        return taskRegistry.findAll().stream()
                .filter(t -> !"DONE".equals(t.getStatus()))
                .collect(Collectors.partitioningBy(
                        t -> t.getDueDate().isBefore(today) // true = overdue
                ));
    }

    // ─────────────────────────────────────────────
    // 3. Top N most urgent tasks
    // ─────────────────────────────────────────────
    public List<Task> topUrgentTasks(int n) {
        return taskRegistry.findAll().stream()
                .filter(t -> !"DONE".equals(t.getStatus()))
                .sorted(Comparator.comparing(Task::getDueDate))
                .limit(n)
                .toList();
    }

    // ─────────────────────────────────────────────
    // 4. Task count grouped by status
    // ─────────────────────────────────────────────
    public Map<String, Long> countByStatus() {
        return taskRegistry.findAll().stream()
                .collect(Collectors.groupingBy(
                        Task::getStatus,  // group by status string
                        Collectors.counting() // count items in each group
                ));
    }

    // ─────────────────────────────────────────────
    // 5. Task count grouped by priority
    // ─────────────────────────────────────────────
    public Map<String, Long> countByPriority() {
        return taskRegistry.findAll().stream()
                .collect(Collectors.groupingBy(
                        Task::getPriority,
                        Collectors.counting()
                ));
    }

    // ─────────────────────────────────────────────
    // 6. Paged task list sorted by due date
    // ─────────────────────────────────────────────
    public PagedResult<Task> getTasksPaged(int page, int pageSize) {
        List<Task> allSorted = taskRegistry.findAll().stream()
                .sorted(Comparator.comparing(Task::getDueDate))
                .toList();

        long total = allSorted.size();
        int fromIndex = page * pageSize;

        // Guard against requesting a page beyond the data
        if (fromIndex >= total) {
            return new PagedResult<>(List.of(), page, pageSize, total);
        }

        int toIndex = (int) Math.min(fromIndex + pageSize, total);
        List<Task> pageContent = allSorted.subList(fromIndex, toIndex);

        return new PagedResult<>(pageContent, page, pageSize, total);
    }

    // ─────────────────────────────────────────────
    // 7. Print a full report to console
    // ─────────────────────────────────────────────
    public void printFullReport() {
        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.println("║       COLLABHUB PROJECT REPORT           ║");
        System.out.println("╚══════════════════════════════════════════╝");

        // Status breakdown
        System.out.println("\n📊 Tasks by Status:");
        countByStatus().forEach((status, count) ->
                System.out.println("   " + status + ": " + count));

        // Priority breakdown
        System.out.println("\n🎯 Tasks by Priority:");
        countByPriority().forEach((priority, count) ->
                System.out.println("   " + priority + ": " + count));

        // Overdue vs on-track
        Map<Boolean, List<Task>> partitioned = partitionByOverdue();
        System.out.println("\n⚠️  Overdue Tasks: " + partitioned.get(true).size());
        System.out.println("✅ On-Track Tasks: " + partitioned.get(false).size());

        // Top 3 urgent
        System.out.println("\n🔥 Top 3 Most Urgent:");
        topUrgentTasks(3).forEach(t ->
                System.out.println("   → " + t.describe()));

        // Assignee summary
        System.out.println("\n👥 Workload by Assignee:");
        generateAssigneeSummary().forEach(s ->
                System.out.println("   " + s.formatted()));

        System.out.println("\n══════════════════════════════════════════\n");
    }
}