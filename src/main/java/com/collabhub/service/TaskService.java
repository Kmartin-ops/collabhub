package com.collabhub.service;

import com.collabhub.async.NotificationDispatcher;
import com.collabhub.async.NotificationEvent;
import com.collabhub.domain.Project;
import com.collabhub.domain.Task;
import com.collabhub.domain.User;
import com.collabhub.exception.ResourceNotFoundException;
import com.collabhub.notification.*;
import com.collabhub.registry.TaskRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final TaskRegistry registry = new TaskRegistry();
    private final Notifiable notifier;
    private NotificationDispatcher dispatcher;

    public TaskService(Notifiable notifier) {
        this.notifier = notifier;
    }

    public void setDispatcher(NotificationDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public Task createTask(String title, String priority, LocalDate dueDate,
                           Project project, User createdBy) {
        log.debug("Creating task '{}' priority={} project={}",
                title, priority, project.getId());
        Task task = new Task(title, priority, dueDate, project);
        registry.save(task);
        log.info("Task created: '{}' id={} project={}",
                title, task.getId(), project.getId());
        dispatchOrNotify(
                NotificationEvent.of(createdBy.getName(),
                        "Task '" + task.getTitle() + "' created in " + project.getName(),
                        "TASK_CREATED"),
                task, createdBy, new TaskCreatedHandler(notifier)
        );
        return task;
    }

    public void assignTask(Task task, User assignee, User assignedBy) {
        log.debug("Assigning task id={} to user={}",
                task.getId(), assignee.getEmail());
        task.setAssignee(assignee);
        registry.save(task);
        log.info("Task assigned: id={} assignee={} by={}",
                task.getId(), assignee.getEmail(), assignedBy.getEmail());
        dispatchOrNotify(
                NotificationEvent.of(assignee.getName(),
                        "You were assigned: '" + task.getTitle() + "'",
                        "TASK_ASSIGNED"),
                task, assignedBy, new TaskAssignedHandler(notifier)
        );
    }

    public void changeStatus(Task task, String newStatus, User changedBy) {
        String oldStatus = task.getStatus();
        log.debug("Changing task status id={} {}→{}",
                task.getId(), oldStatus, newStatus);
        task.setStatus(newStatus);
        log.info("Task status changed: id='{}' {}→{} by={}",
                task.getTitle(), oldStatus, newStatus, changedBy.getEmail());
        if ("DONE".equals(newStatus)) {
            dispatchOrNotify(
                    NotificationEvent.of(changedBy.getName(),
                            "🎉 '" + task.getTitle() + "' is DONE!",
                            "TASK_COMPLETED"),
                    task, changedBy, new TaskCompletedHandler(notifier)
            );
        }
    }

    public Task getById(UUID id) {
        return registry.findById(id)
                .orElseThrow(() -> {
                    log.warn("Task not found: id={}", id);
                    return new ResourceNotFoundException("Task", id);
                });
    }

    private void dispatchOrNotify(NotificationEvent event, Task task,
                                  User actor, StatusChangeHandler handler) {
        if (dispatcher != null) {
            dispatcher.dispatch(event);
        } else {
            handler.handle(task, actor);
        }
    }

    public Optional<Task> findById(UUID id)             { return registry.findById(id); }
    public List<Task> findByProject(Project project)    { return registry.findByProject(project); }
    public List<Task> findByAssignee(User user)         { return registry.findByAssignee(user); }
    public List<Task> findOverdue()                     { return registry.findOverdue(); }
    public List<Task> findAllSortedByDueDate()          { return registry.findAllSortedByDueDate(); }
    public Optional<Task> getNextUrgentTask()           { return registry.getNextUrgent(); }
    public TaskRegistry getRegistry()                   { return registry; }
}