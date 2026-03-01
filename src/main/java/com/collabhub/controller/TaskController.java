package com.collabhub.controller;

import com.collabhub.dto.CreateTaskRequest;
import com.collabhub.dto.TaskResponse;
import com.collabhub.dto.UpdateTaskRequest;
import com.collabhub.mapper.TaskMapper;
import com.collabhub.service.ProjectService;
import com.collabhub.service.TaskService;
import com.collabhub.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Tasks", description = "Task management endpoints")
public class TaskController {

    private final TaskService    taskService;
    private final ProjectService projectService;
    private final UserService    userService;
    private final TaskMapper     taskMapper;

    public TaskController(TaskService taskService,
                          ProjectService projectService,
                          UserService userService,
                          TaskMapper taskMapper) {
        this.taskService    = taskService;
        this.projectService = projectService;
        this.userService    = userService;
        this.taskMapper     = taskMapper;
    }

    // GET /api/tasks?projectId=xxx
    @GetMapping
    @Operation(summary = "Get tasks",
            description = "Returns tasks for a project, or all tasks if no projectId given")
    @ApiResponse(responseCode = "200", description = "Tasks returned")
    public List<TaskResponse> getTasks(
            @RequestParam(required = false) UUID projectId) {

        var tasks = projectId != null
                ? taskService.findByProject(projectId)
                : taskService.findAll();

        return tasks.stream()
                .map(taskMapper::toResponse)
                .toList();
    }

    // GET /api/tasks/{id}
    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task found"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    public TaskResponse getById(@PathVariable UUID id) {
        return taskMapper.toResponse(taskService.getById(id));
    }

    // GET /api/tasks/overdue
    @GetMapping("/overdue")
    @Operation(summary = "Get overdue tasks",
            description = "Returns all tasks past their due date that are not DONE")
    public List<TaskResponse> getOverdue() {
        return taskService.findOverdue().stream()
                .map(taskMapper::toResponse)
                .toList();
    }

    // POST /api/tasks
    @PostMapping
    @Operation(summary = "Create a new task")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Task created"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "404", description = "Project or creator not found")
    })
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody CreateTaskRequest request) {

        var project = projectService.getById(request.projectId());
        var creator = userService.getByEmail(request.creatorEmail());
        var task    = taskService.createTask(
                request.title(), request.priority(),
                request.dueDate(), project, creator);

        return ResponseEntity
                .created(URI.create("/api/tasks/" + task.getId()))
                .body(taskMapper.toResponse(task));
    }

    // PUT /api/tasks/{id}
    @PutMapping("/{id}")
    @Operation(summary = "Update a task",
            description = "Updates title, status, priority, dueDate, or assignee. Null fields ignored.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task updated"),
            @ApiResponse(responseCode = "404", description = "Task or assignee not found")
    })
    public TaskResponse updateTask(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTaskRequest request) {

        var task = taskService.getById(id);

        if (request.title()    != null) task.setTitle(request.title());
        if (request.priority() != null) task.setPriority(request.priority());
        if (request.dueDate()  != null) task.setDueDate(request.dueDate());

        if (request.assigneeEmail() != null) {
            var assignee = userService.getByEmail(request.assigneeEmail());
            taskService.assignTask(task, assignee, assignee);
        }

        if (request.status() != null) {
            var actor = task.getAssignee() != null
                    ? task.getAssignee()
                    : userService.findAll().get(0);
            taskService.changeStatus(task, request.status(), actor);
        }

        return taskMapper.toResponse(taskService.getById(id));
    }

    // DELETE /api/tasks/{id}
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a task")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Task deleted"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    public ResponseEntity<Void> deleteTask(@PathVariable UUID id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}