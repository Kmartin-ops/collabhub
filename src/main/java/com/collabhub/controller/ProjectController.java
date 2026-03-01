package com.collabhub.controller;

import com.collabhub.dto.*;
import com.collabhub.mapper.ProjectMapper;
import com.collabhub.service.ProjectService;
import com.collabhub.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
    private final ProjectMapper  projectMapper;

    public ProjectController(ProjectService projectService,
                             UserService userService,
                             ProjectMapper projectMapper) {
        this.projectService = projectService;
        this.userService    = userService;
        this.projectMapper  = projectMapper;
    }

    @GetMapping
    @Operation(summary = "Get all projects")
    public List<ProjectResponse> getAllProjects(
            @Parameter(description = "Filter by status: ACTIVE, COMPLETED, ARCHIVED")
            @RequestParam(required = false) String status) {

        return projectService.getAllProjects().stream()
                .filter(p -> status == null
                        || p.getStatus().equalsIgnoreCase(status))
                .map(projectMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get project by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Project found"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    public ProjectResponse getById(@PathVariable UUID id) {
        return projectMapper.toResponse(projectService.getById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new project")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Project created"),
            @ApiResponse(responseCode = "400", description = "Validation failed or creator not found")
    })
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody CreateProjectRequest request) {
        var creator = userService.getByEmail(request.creatorEmail());
        var project = projectService.createProject(
                request.name(), request.description(), creator);
        return ResponseEntity
                .created(URI.create("/api/projects/" + project.getId()))
                .body(projectMapper.toResponse(project));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a project")
    public ProjectResponse updateProject(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProjectRequest request) {
        var project = projectService.getById(id);
        return projectMapper.toResponse(projectService.updateProject(
                project, request.name(), request.description(), request.status()));
    }

    @PostMapping("/{id}/members")
    @Operation(summary = "Add a member to a project")
    public ProjectResponse addMember(
            @PathVariable UUID id,
            @Valid @RequestBody AddMemberRequest request) {
        var project = projectService.getById(id);
        var user    = userService.getByEmail(request.userEmail());
        projectService.addMember(project, user);
        return projectMapper.toResponse(projectService.getById(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a project")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Deleted"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<Void> deleteProject(@PathVariable UUID id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
}