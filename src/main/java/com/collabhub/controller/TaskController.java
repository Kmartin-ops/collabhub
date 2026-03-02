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

    // GET /api/tasks?status=IN_PROGRESS&priority=HIGH&keyword=login&page=0&size=20
    @GetMapping
    @Operation(
            summary = "Search tasks with filters and pagination",
            description = "Filter by status, priority, projectId, assigneeId, or keyword. " +
                    "Sort with sort=dueDate,asc. Page with page=0&size=20."
    )
    @ApiResponse(responseCode = "200", description = "Paginated task results")
    public PagedResponse<TaskResponse> searchTasks(

            @Parameter(description = "Filter by status")
            @RequestParam(required = false) String status,

            @Parameter(description = "Filter by priority")
            @RequestParam(required = false) String priority,

            @Parameter(description = "Filter by project ID")
            @RequestParam(required = false) UUID projectId,

            @Parameter(description = "Filter by assignee ID")
            @RequestParam(required = false) UUID assigneeId,

            @Parameter(description = "Search in title")
            @RequestParam(required = false) String keyword,

            // Default: page 0, 20 per page, sorted by dueDate ascending
            @PageableDefault(size = 20, sort = "dueDate",
                    direction = Sort.Direction.ASC)
            Pageable pageable) {

        var page = taskService.search(
                status, priority, projectId, assigneeId, keyword, pageable);

        var content = page.getContent().stream()
                .map(taskMapper::toResponse)
                .toList();

        return PagedResponse.from(
                new org.springframework.data.domain.PageImpl<>(
                        content, pageable, page.getTotalElements()));
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
    @Operation(summary = "Get all overdue tasks")
    public List<TaskResponse> getOverdue() {
        return taskService.findOverdue().stream()
                .map(taskMapper::toResponse)
                .toList();
    }

    // GET /api/tasks/board?projectId=xxx
    @GetMapping("/board")
    @Operation(
            summary = "Get Kanban board for a project",
            description = "Returns tasks sorted by priority then due date for board display"
    )
    public List<TaskResponse> getBoard(
            @Parameter(description = "Project ID", required = true)
            @RequestParam UUID projectId) {
        return taskService.findKanbanBoard(projectId).stream()
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
    @Operation(summary = "Update a task")
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
            @ApiResponse(responseCode = "204", description = "Deleted"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<Void> deleteTask(@PathVariable UUID id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}