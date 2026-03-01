package com.collabhub.service;

import com.collabhub.async.NotificationDispatcher;
import com.collabhub.async.NotificationEvent;
import com.collabhub.domain.Project;
import com.collabhub.domain.Task;
import com.collabhub.domain.User;
import com.collabhub.exception.ResourceNotFoundException;
import com.collabhub.notification.*;
import com.collabhub.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository   taskRepository;
    private final Notifiable       notifier;
    private NotificationDispatcher dispatcher;

    public TaskService(TaskRepository taskRepository, Notifiable notifier) {
        this.taskRepository = taskRepository;
        this.notifier       = notifier;
    }

    public void setDispatcher(NotificationDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Transactional
    public Task createTask(String title, String priority, LocalDate dueDate,
                           Project project, User createdBy) {
        log.debug("Creating task '{}' priority={} project={}",
                title, priority, project.getId());
        Task task = new Task(title, priority, dueDate, project);
        Task saved = taskRepository.save(task);
        log.info("Task created: '{}' id={} project={}",
                title, saved.getId(), project.getId());
        dispatchOrNotify(
                NotificationEvent.of(createdBy.getName(),
                        "Task '" + saved.getTitle() + "' created in "
                                + project.getName(), "TASK_CREATED"),
                saved, createdBy, new TaskCreatedHandler(notifier)
        );
        return saved;
    }

    @Transactional
    public void assignTask(Task task, User assignee, User assignedBy) {
        log.debug("Assigning task id={} to user={}",
                task.getId(), assignee.getEmail());
        task.setAssignee(assignee);
        taskRepository.save(task);
        log.info("Task assigned: id={} assignee={} by={}",
                task.getId(), assignee.getEmail(), assignedBy.getEmail());
        dispatchOrNotify(
                NotificationEvent.of(assignee.getName(),
                        "You were assigned: '" + task.getTitle() + "'",
                        "TASK_ASSIGNED"),
                task, assignedBy, new TaskAssignedHandler(notifier)
        );
    }

    @Transactional
    public void changeStatus(Task task, String newStatus, User changedBy) {
        String oldStatus = task.getStatus();
        task.setStatus(newStatus);
        taskRepository.save(task);
        log.info("Task status changed: '{}' {}→{} by={}",
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

    @Transactional(readOnly = true)
    public Task getById(UUID id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Task not found: id={}", id);
                    return new ResourceNotFoundException("Task", id);
                });
    }

    @Transactional(readOnly = true)
    public List<Task> findByProject(UUID projectId) {
        return taskRepository.findByProjectIdWithAssignee(projectId);
    }

    @Transactional(readOnly = true)
    public List<Task> findKanbanBoard(UUID projectId) {
        return taskRepository.findKanbanBoard(projectId);
    }

    @Transactional(readOnly = true)
    public List<Task> findByAssignee(UUID assigneeId) {
        return taskRepository.findByAssigneeId(assigneeId);
    }

    @Transactional(readOnly = true)
    public List<Task> findOverdue() {
        return taskRepository.findByDueDateBeforeAndStatusNot(
                LocalDate.now(), "DONE");
    }

    @Transactional(readOnly = true)
    public List<Task> findAll() {
        return taskRepository.findAll();
    }

    private void dispatchOrNotify(NotificationEvent event, Task task,
                                  User actor, StatusChangeHandler handler) {
        if (dispatcher != null) {
            dispatcher.dispatch(event);
        } else {
            handler.handle(task, actor);
        }
    }
}