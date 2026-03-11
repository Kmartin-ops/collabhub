package com.collabhub.report;

import com.collabhub.domain.Task;
import com.collabhub.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportEngine {

    private static final Logger LOG = LoggerFactory.getLogger(ReportEngine.class);

    private final TaskRepository taskRepository;

    public ReportEngine(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<TaskSummary> generateAssigneeSummary() {
        LocalDate today = LocalDate.now();

        Map<String, List<Task>> byAssignee = taskRepository.findAll().stream()
                .filter(t -> t.getAssignee() != null)
                .collect(Collectors.groupingBy(t -> t.getAssignee().getName()));

        return byAssignee.entrySet().stream().map(entry -> {
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
                }).sorted(Comparator.comparing(TaskSummary::totalTasks).reversed())
                .toList();
    }

    public Map<Boolean, List<Task>> partitionByOverdue() {
        LocalDate today = LocalDate.now();
        return taskRepository.findAll().stream()
                .filter(t -> !"DONE".equals(t.getStatus()))
                .collect(Collectors.partitioningBy(t -> t.getDueDate().isBefore(today)));
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

        int toIndex = (int) Math.min((long) fromIndex + pageSize, total);
        return new PagedResult<>(allSorted.subList(fromIndex, toIndex), page, pageSize, total);
    }

    public void printFullReport() {
        LOG.info("\n╔══════════════════════════════════════════╗");
        LOG.info("║       COLLABHUB PROJECT REPORT           ║");
        LOG.info("╚══════════════════════════════════════════╝");

        LOG.info("\n📊 Tasks by Status:");
        countByStatus().forEach((s, c) -> LOG.info("   {}: {}", s, c));

        LOG.info("\n🎯 Tasks by Priority:");
        countByPriority().forEach((p, c) -> LOG.info("   {}: {}", p, c));

        Map<Boolean, List<Task>> partitioned = partitionByOverdue();
        LOG.info("\n⚠️  Overdue: {}", partitioned.get(true).size());
        LOG.info("✅ On-Track: {}", partitioned.get(false).size());

        LOG.info("\n🔥 Top 3 Urgent:");
        topUrgentTasks(3).forEach(t -> LOG.info("   → {}", t.describe()));

        LOG.info("\n👥 Workload by Assignee:");
        generateAssigneeSummary().forEach(s -> LOG.info("   {}", s.formatted()));

        LOG.info("\n══════════════════════════════════════════\n");
    }
}