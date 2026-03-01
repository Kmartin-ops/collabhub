package com.collabhub.controller;

import com.collabhub.dto.*;
import com.collabhub.service.ProjectService;
import com.collabhub.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final UserService    userService;

    public ProjectController(ProjectService projectService, UserService userService) {
        this.projectService = projectService;
        this.userService    = userService;
    }

    @GetMapping
    public List<ProjectResponse> getAllProjects(
            @RequestParam(required = false) String status) {

        var all = projectService.getAllProjects();
        return all.stream()
                .filter(p -> status == null
                        || p.getStatus().equalsIgnoreCase(status))
                .map(ProjectResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public ProjectResponse getById(@PathVariable UUID id) {
        return ProjectResponse.from(projectService.getById(id));
    }

    @PostMapping
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
    public ProjectResponse updateProject(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProjectRequest request) {

        var project = projectService.getById(id);
        return ProjectResponse.from(projectService.updateProject(
                project, request.name(), request.description(), request.status()));
    }

    @PostMapping("/{id}/members")
    public ProjectResponse addMember(
            @PathVariable UUID id,
            @Valid @RequestBody AddMemberRequest request) {

        var project = projectService.getById(id);
        var user    = userService.getByEmail(request.userEmail());
        projectService.addMember(project, user);
        return ProjectResponse.from(project);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable UUID id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
}