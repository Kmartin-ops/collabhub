package com.collabhub.service;

import com.collabhub.async.NotificationDispatcher;
import com.collabhub.async.NotificationEvent;
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
    private NotificationDispatcher dispatcher; // optional async dispatcher

    public TaskService(Notifiable notifier) {
        this.notifier = notifier;
    }

    // Attach dispatcher after construction
    public void setDispatcher(NotificationDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public Task createTask(String title, String priority, LocalDate dueDate,
                           Project project, User createdBy) {
        Task task = new Task(title, priority, dueDate, project);
        registry.save(task);

        dispatchOrNotify(
                NotificationEvent.of(createdBy.getName(),
                        "Task '" + task.getTitle() + "' created in " + project.getName(),
                        "TASK_CREATED"),
                task, createdBy, new TaskCreatedHandler(notifier)
        );
        return task;
    }

    public void assignTask(Task task, User assignee, User assignedBy) {
        task.setAssignee(assignee);
        registry.save(task);

        dispatchOrNotify(
                NotificationEvent.of(assignee.getName(),
                        "You were assigned: '" + task.getTitle() + "'",
                        "TASK_ASSIGNED"),
                task, assignedBy, new TaskAssignedHandler(notifier)
        );
    }

    public void changeStatus(Task task, String newStatus, User changedBy) {
        String oldStatus = task.getStatus();
        task.setStatus(newStatus);
        System.out.println("[TaskService] " + task.getTitle()
                + ": " + oldStatus + " → " + newStatus);

        if ("DONE".equals(newStatus)) {
            dispatchOrNotify(
                    NotificationEvent.of(changedBy.getName(),
                            "🎉 '" + task.getTitle() + "' is DONE!",
                            "TASK_COMPLETED"),
                    task, changedBy, new TaskCompletedHandler(notifier)
            );
        }
    }

    // If dispatcher is set, go async. Otherwise fall back to sync.
    private void dispatchOrNotify(NotificationEvent event, Task task,
                                  User actor, StatusChangeHandler handler) {
        if (dispatcher != null) {
            dispatcher.dispatch(event);
        } else {
            handler.handle(task, actor);
        }
    }

    public Optional<Task> findById(UUID id) { return registry.findById(id); }
    public List<Task> findByProject(Project project) { return registry.findByProject(project); }
    public List<Task> findByAssignee(User user) { return registry.findByAssignee(user); }
    public List<Task> findOverdue() { return registry.findOverdue(); }
    public List<Task> findAllSortedByDueDate() { return registry.findAllSortedByDueDate(); }
    public Optional<Task> getNextUrgentTask() { return registry.getNextUrgent(); }
    public TaskRegistry getRegistry() { return registry; }
}