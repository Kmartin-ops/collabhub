package com.collabhub.service;

import com.collabhub.domain.Project;
import com.collabhub.domain.Task;
import com.collabhub.domain.User;
import com.collabhub.notification.*;
import com.collabhub.registry.TaskRegistry;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TaskService {

    private final TaskRegistry registry = new TaskRegistry();
    private final Notifiable notifier;

    public TaskService(Notifiable notifier) {
        this.notifier = notifier;
    }

    public Task createTask(String title, String priority, LocalDate dueDate,
                           Project project, User createdBy) {
        Task task = new Task(title, priority, dueDate, project);
        registry.save(task);
        new TaskCreatedHandler(notifier).handle(task, createdBy);
        return task;
    }

    public void assignTask(Task task, User assignee, User assignedBy) {
        task.setAssignee(assignee);
        registry.save(task);
        new TaskAssignedHandler(notifier).handle(task, assignedBy);
    }

    public void changeStatus(Task task, String newStatus, User changedBy) {
        String oldStatus = task.getStatus();
        task.setStatus(newStatus);
        System.out.println("[TaskService] " + task.getTitle()
                + ": " + oldStatus + " → " + newStatus);
        if ("DONE".equals(newStatus)) {
            new TaskCompletedHandler(notifier).handle(task, changedBy);
        }
    }

    public Optional<Task> findById(UUID id) {
        return registry.findById(id);
    }

    public List<Task> findByProject(Project project) {
        return registry.findByProject(project);
    }

    public List<Task> findByAssignee(User user) {
        return registry.findByAssignee(user);
    }

    public List<Task> findOverdue() {
        return registry.findOverdue();
    }

    public List<Task> findAllSortedByDueDate() {
        return registry.findAllSortedByDueDate();
    }

    public Optional<Task> getNextUrgentTask() {
        return registry.getNextUrgent();
    }
}