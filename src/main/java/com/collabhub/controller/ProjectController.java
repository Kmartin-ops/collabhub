package com.collabhub.controller;

import com.collabhub.dto.*;
import com.collabhub.service.ProjectService;
import com.collabhub.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
@RequestMapping("/api/projects")
@Tag(name = "Projects", description = "Project management endpoints")
public class ProjectController {

    private final ProjectService projectService;
    private final UserService    userService;

    public ProjectController(ProjectService projectService,
                             UserService userService) {
        this.projectService = projectService;
        this.userService    = userService;
    }

    @GetMapping
    @Operation(
            summary = "Get all projects",
            description = "Returns all projects, optionally filtered by status"
    )
    @ApiResponse(responseCode = "200", description = "Projects returned")
    public List<ProjectResponse> getAllProjects(
            @Parameter(description = "Filter by status: ACTIVE, COMPLETED, ARCHIVED")
            @RequestParam(required = false) String status) {

        return projectService.getAllProjects().stream()
                .filter(p -> status == null
                        || p.getStatus().equalsIgnoreCase(status))
                .map(ProjectResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get project by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Project found"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Project not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ProjectResponse getById(
            @Parameter(description = "Project UUID")
            @PathVariable UUID id) {
        return ProjectResponse.from(projectService.getById(id));
    }

    @PostMapping
    @Operation(
            summary = "Create a new project",
            description = "Creates a project and automatically adds the creator as a member"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Project created"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed or creator not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody CreateProjectRequest request) {
        var creator = userService.getByEmail(request.creatorEmail());
        var project = projectService.createProject(
                request.name(), request.description(), creator);
        return ResponseEntity
                .created(URI.create("/api/projects/" + project.getId()))
                .body(ProjectResponse.from(project));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update a project",
            description = "Updates name, description, or status. Null fields are ignored."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Project updated"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Project not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ProjectResponse updateProject(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProjectRequest request) {
        var project = projectService.getById(id);
        return ProjectResponse.from(projectService.updateProject(
                project, request.name(), request.description(), request.status()));
    }

    @PostMapping("/{id}/members")
    @Operation(
            summary = "Add a member to a project",
            description = "Adds an existing user to the project. Duplicates are ignored."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Member added"),
            @ApiResponse(responseCode = "404", description = "Project or user not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ProjectResponse addMember(
            @PathVariable UUID id,
            @Valid @RequestBody AddMemberRequest request) {
        var project = projectService.getById(id);
        var user    = userService.getByEmail(request.userEmail());
        projectService.addMember(project, user);
        return ProjectResponse.from(project);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a project")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Project deleted"),
            @ApiResponse(responseCode = "404", description = "Project not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteProject(@PathVariable UUID id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
}