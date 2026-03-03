package com.collabhub.controller;

import com.collabhub.dto.CreateProjectRequest;
import com.collabhub.dto.ProjectResponse;
import com.collabhub.dto.UpdateProjectRequest;
import com.collabhub.mapper.ProjectMapper;
import com.collabhub.service.ProjectService;
import com.collabhub.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
    public List<ProjectResponse> getAllProjects() {
        return projectService.getAllProjects().stream()
                .map(projectMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get project by ID")
    public ProjectResponse getById(@PathVariable UUID id) {
        return projectMapper.toResponse(projectService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")  // ← MANAGER only
    @Operation(summary = "Create a new project")
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        var creator = userService.getByEmail(userDetails.getUsername());
        var project = projectService.createProject(
                request.name(), request.description(), creator);
        return ResponseEntity
                .created(URI.create("/api/projects/" + project.getId()))
                .body(projectMapper.toResponse(project));
    }

    @PostMapping("/{id}/members")
    @PreAuthorize("hasRole('MANAGER')")  // ← MANAGER only
    @Operation(summary = "Add a member to a project")
    public ProjectResponse addMember(
            @PathVariable UUID id,
            @RequestParam String email) {
        var project = projectService.getById(id);
        var user    = userService.getByEmail(email);
        projectService.addMember(project, user);
        return projectMapper.toResponse(projectService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")  // ← MANAGER only
    @Operation(summary = "Update a project")
    public ProjectResponse updateProject(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProjectRequest request) {
        var project = projectService.getById(id);
        projectService.updateProject(
                project, request.name(), request.description(), request.status());
        return projectMapper.toResponse(projectService.getById(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")  // ← MANAGER only
    @Operation(summary = "Delete a project")
    public ResponseEntity<Void> deleteProject(@PathVariable UUID id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
}