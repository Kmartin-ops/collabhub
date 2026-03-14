package com.collabhub.service;

import com.collabhub.async.NotificationDispatcher;
import com.collabhub.async.NotificationEvent;
import com.collabhub.domain.Project;
import com.collabhub.domain.Task;
import com.collabhub.domain.User;
import com.collabhub.exception.ResourceNotFoundException;
import com.collabhub.notification.Notifiable;
import com.collabhub.notification.StatusChangeHandler;
import com.collabhub.notification.TaskAssignedHandler;
import com.collabhub.notification.TaskCompletedHandler;
import com.collabhub.notification.TaskCreatedHandler;
import com.collabhub.repository.TaskRepository;
import com.collabhub.repository.TaskSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class TaskService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository   taskRepository;
    private final Notifiable       notifier;
    private final ActivityService  activityService;
    private NotificationDispatcher dispatcher;

    public TaskService(TaskRepository taskRepository,
                       Notifiable notifier,
                       ActivityService activityService) {
        this.taskRepository  = taskRepository;
        this.notifier        = notifier;
        this.activityService = activityService;
    }

    public void setDispatcher(NotificationDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Transactional
    public Task createTask(String title, String priority, LocalDate dueDate, Project project, User createdBy) {
        LOG.debug("Creating task '{}' priority={} project={}", title, priority, project.getId());
        Task task  = new Task(title, priority, dueDate, project);
        Task saved = taskRepository.save(task);
        LOG.info("Task created: '{}' id={} project={}", title, saved.getId(), project.getId());

        activityService.log("TASK_CREATED", createdBy.getName(),
                "TASK", saved.getId(), saved.getTitle(),
                "Priority: " + priority, project);

        dispatchOrNotify(
                NotificationEvent.of(createdBy.getName(),
                        "Task '" + saved.getTitle() + "' created in " + project.getName(), "TASK_CREATED"),
                saved, createdBy, new TaskCreatedHandler(notifier));
        return saved;
    }

    @Transactional
    public void assignTask(Task task, User assignee, User assignedBy) {
        LOG.debug("Assigning task id={} to user={}", task.getId(), assignee.getEmail());
        task.setAssignee(assignee);
        taskRepository.save(task);
        LOG.info("Task assigned: id={} assignee={} by={}", task.getId(), assignee.getEmail(), assignedBy.getEmail());

        activityService.log("TASK_ASSIGNED", assignedBy.getName(),
                "TASK", task.getId(), task.getTitle(),
                "Assigned to " + assignee.getName(), task.getProject());

        dispatchOrNotify(NotificationEvent.of(assignee.getName(),
                "You were assigned: '" + task.getTitle() + "'", "TASK_ASSIGNED"),
                task, assignedBy, new TaskAssignedHandler(notifier));
    }

    @Transactional
    public void changeStatus(Task task, String newStatus, User changedBy) {
        String oldStatus = task.getStatus();
        task.setStatus(newStatus);
        taskRepository.save(task);
        LOG.info("Task status changed: '{}' {}to{} by={}", task.getTitle(), oldStatus, newStatus, changedBy.getEmail());

        activityService.log("STATUS_CHANGED", changedBy.getName(),
                "TASK", task.getId(), task.getTitle(),
                oldStatus + " to " + newStatus, task.getProject());

        if ("DONE".equals(newStatus)) {
            dispatchOrNotify(NotificationEvent.of(changedBy.getName(),
                    "Task '" + task.getTitle() + "' is DONE!", "TASK_COMPLETED"),
                    task, changedBy, new TaskCompletedHandler(notifier));
        }
    }

    public Task getById(UUID id) {
        return taskRepository.findById(id).orElseThrow(() -> {
            LOG.warn("Task not found: id={}", id);
            return new ResourceNotFoundException("Task", id);
        });
    }

    @Transactional(readOnly = true)
    public Page<Task> search(String status, String priority, UUID projectId, UUID assigneeId, String keyword,
            Pageable pageable) {
        var spec = TaskSpecification.withFilters(status, priority, projectId, assigneeId, keyword);
        return taskRepository.findAll(spec, pageable);
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
        return taskRepository.findByDueDateBeforeAndStatusNot(LocalDate.now(), "DONE");
    }

    @Transactional(readOnly = true)
    public List<Task> findAll() {
        return taskRepository.findAll();
    }

    @Transactional
    public void deleteTask(UUID id) {
        LOG.debug("Deleting task id={}", id);
        Task task = getById(id);
        taskRepository.delete(task);
        LOG.info("Task deleted: id={}", id);
    }

    private void dispatchOrNotify(NotificationEvent event, Task task, User actor, StatusChangeHandler handler) {
        if (dispatcher != null) {
            dispatcher.dispatch(event);
        } else {
            handler.handle(task, actor);
        }
    }
}
