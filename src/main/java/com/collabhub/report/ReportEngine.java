package com.collabhub.report;

import com.collabhub.domain.Task;
import com.collabhub.repository.TaskRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class ReportEngine {

    private final TaskRepository taskRepository;

    public ReportEngine(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<TaskSummary> generateAssigneeSummary() {
        LocalDate today = LocalDate.now();

        Map<String, List<Task>> byAssignee = taskRepository.findAll().stream()
                .filter(t -> t.getAssignee() != null)
                .collect(Collectors.groupingBy(t -> t.getAssignee().getName()));

        return byAssignee.entrySet().stream()
                .map(entry -> {
                    String name = entry.getKey();
                    List<Task> tasks = entry.getValue();

                    long overdue = tasks.stream()
                            .filter(t -> t.getDueDate().isBefore(today))
                            .filter(t -> !"DONE".equals(t.getStatus()))
                            .count();

                    long done = tasks.stream()
                            .filter(t -> "DONE".equals(t.getStatus()))
                            .count();

                    long inProgress = tasks.stream()
                            .filter(t -> "IN_PROGRESS".equals(t.getStatus()))
                            .count();

                    return new TaskSummary(name, tasks.size(), overdue, done, inProgress);
                })
                .sorted(Comparator.comparing(TaskSummary::totalTasks).reversed())
                .toList();
    }

    public Map<Boolean, List<Task>> partitionByOverdue() {
        LocalDate today = LocalDate.now();
        return taskRepository.findAll().stream()
                .filter(t -> !"DONE".equals(t.getStatus()))
                .collect(Collectors.partitioningBy(
                        t -> t.getDueDate().isBefore(today)));
    }

    public List<Task> topUrgentTasks(int n) {
        return taskRepository.findAll().stream()
                .filter(t -> !"DONE".equals(t.getStatus()))
                .sorted(Comparator.comparing(Task::getDueDate))
                .limit(n)
                .toList();
    }

    public Map<String, Long> countByStatus() {
        return taskRepository.findAll().stream()
                .collect(Collectors.groupingBy(Task::getStatus, Collectors.counting()));
    }

    public Map<String, Long> countByPriority() {
        return taskRepository.findAll().stream()
                .collect(Collectors.groupingBy(Task::getPriority, Collectors.counting()));
    }

    public PagedResult<Task> getTasksPaged(int page, int pageSize) {
        List<Task> allSorted = taskRepository.findAll().stream()
                .sorted(Comparator.comparing(Task::getDueDate))
                .toList();

        long total = allSorted.size();
        int fromIndex = page * pageSize;

        if (fromIndex >= total) {
            return new PagedResult<>(List.of(), page, pageSize, total);
        }

        int toIndex = (int) Math.min(fromIndex + pageSize, total);
        return new PagedResult<>(allSorted.subList(fromIndex, toIndex),
                page, pageSize, total);
    }

    public void printFullReport() {
        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.println("║       COLLABHUB PROJECT REPORT           ║");
        System.out.println("╚══════════════════════════════════════════╝");

        System.out.println("\n📊 Tasks by Status:");
        countByStatus().forEach((s, c) -> System.out.println("   " + s + ": " + c));

        System.out.println("\n🎯 Tasks by Priority:");
        countByPriority().forEach((p, c) -> System.out.println("   " + p + ": " + c));

        Map<Boolean, List<Task>> partitioned = partitionByOverdue();
        System.out.println("\n⚠️  Overdue: " + partitioned.get(true).size());
        System.out.println("✅ On-Track: " + partitioned.get(false).size());

        System.out.println("\n🔥 Top 3 Urgent:");
        topUrgentTasks(3).forEach(t -> System.out.println("   → " + t.describe()));

        System.out.println("\n👥 Workload by Assignee:");
        generateAssigneeSummary().forEach(s ->
                System.out.println("   " + s.formatted()));

        System.out.println("\n══════════════════════════════════════════\n");
    }
}