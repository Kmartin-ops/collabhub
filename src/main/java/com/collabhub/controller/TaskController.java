package com.collabhub.controller;

import com.collabhub.dto.CreateTaskRequest;
import com.collabhub.dto.PagedResponse;
import com.collabhub.dto.TaskResponse;
import com.collabhub.dto.UpdateTaskRequest;
import com.collabhub.mapper.TaskMapper;
import com.collabhub.service.ProjectService;
import com.collabhub.service.TaskService;
import com.collabhub.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Tasks", description = "Task management endpoints")
public class TaskController {

    private final TaskService taskService;
    private final ProjectService projectService;
    private final UserService userService;
    private final TaskMapper taskMapper;

    public TaskController(TaskService taskService, ProjectService projectService, UserService userService,
            TaskMapper taskMapper) {
        this.taskService = taskService;
        this.projectService = projectService;
        this.userService = userService;
        this.taskMapper = taskMapper;
    }


    @GetMapping
    @Operation(summary = "Search tasks with filters and pagination", description = "Filter by status, priority, projectId, assigneeId, or keyword. "
            + "Sort with sort=dueDate,asc. Page with page=0&size=20.")
    public PagedResponse<TaskResponse> searchTasks(

            @Parameter(description = "Filter by status") @RequestParam(required = false) String status,

            @Parameter(description = "Filter by priority") @RequestParam(required = false) String priority,

            @Parameter(description = "Filter by project ID") @RequestParam(required = false) UUID projectId,

            @Parameter(description = "Filter by assignee ID") @RequestParam(required = false) UUID assigneeId,

            @Parameter(description = "Search in title") @RequestParam(required = false) String keyword,


            @PageableDefault(size = 20, sort = "dueDate", direction = Sort.Direction.ASC) Pageable pageable) {

        var page = taskService.search(status, priority, projectId, assigneeId, keyword, pageable);

        var content = page.getContent().stream().map(taskMapper::toResponse).toList();

        return PagedResponse
                .from(new org.springframework.data.domain.PageImpl<>(content, pageable, page.getTotalElements()));
    }


    public TaskResponse getById(@PathVariable UUID id) {
        return taskMapper.toResponse(taskService.getById(id));
    }


    @GetMapping("/overdue")
    @Operation(summary = "Get all overdue tasks")
    public List<TaskResponse> getOverdue() {
        return taskService.findOverdue().stream().map(taskMapper::toResponse).toList();
    }


    @GetMapping("/board")
    @Operation(summary = "Get Kanban board for a project", description = "Returns tasks sorted by priority then due date for board display")
    public List<TaskResponse> getBoard(
            @Parameter(description = "Project ID", required = true) @RequestParam UUID projectId) {
        return taskService.findKanbanBoard(projectId).stream().map(taskMapper::toResponse).toList();
    }


    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Create a new task")
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest request) {
        var project = projectService.getById(request.projectId());
        var creator = userService.getByEmail(request.creatorEmail());
        var task = taskService.createTask(request.title(), request.priority(), request.dueDate(), project, creator);
        return ResponseEntity.created(URI.create("/api/tasks/" + task.getId())).body(taskMapper.toResponse(task));
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or (hasRole('DEVELOPER') and @taskAuthService.isAssignee(#id, authentication.name))")
    @Operation(summary = "Update a task")
    public TaskResponse updateTask(@PathVariable UUID id, @Valid @RequestBody UpdateTaskRequest request) {
        var task = taskService.getById(id);
        if (request.title() != null) {
            task.setTitle(request.title());
        }
        if (request.priority() != null) {
            task.setPriority(request.priority());
        }
        if (request.dueDate() != null) {
            task.setDueDate(request.dueDate());
        }
        if (request.assigneeEmail() != null) {
            var assignee = userService.getByEmail(request.assigneeEmail());
            taskService.assignTask(task, assignee, assignee);
        }
        if (request.status() != null) {
            var actor = task.getAssignee() != null ? task.getAssignee() : userService.findAll().get(0);
            taskService.changeStatus(task, request.status(), actor);
        }
        return taskMapper.toResponse(taskService.getById(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Delete a task")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
