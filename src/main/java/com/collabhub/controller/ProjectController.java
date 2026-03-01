package com.collabhub.controller;

import com.collabhub.dto.*;
import com.collabhub.service.ProjectService;
import com.collabhub.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final UserService userService;

    public ProjectController(ProjectService projectService, UserService userService) {
        this.projectService = projectService;
        this.userService    = userService;
    }

    // GET /api/projects
    @GetMapping
    public List<ProjectResponse> getAllProjects(
            @RequestParam(required = false) String status) {

        var projects = status != null
                ? projectService.getAllProjects().stream()
                .filter(p -> p.getStatus().equalsIgnoreCase(status))
                .toList()
                : projectService.getAllProjects().stream().toList();

        return projects.stream()
                .map(ProjectResponse::from)
                .toList();
    }

    // GET /api/projects/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getById(@PathVariable UUID id) {
        return projectService.findById(id)
                .map(ProjectResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/projects → 201 Created
    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @RequestBody CreateProjectRequest request) {

        // Look up the creator by email
        var creator = userService.findByEmail(request.creatorEmail());
        if (creator.isEmpty()) {
            return ResponseEntity.badRequest().build(); // 400 — unknown creator
        }

        var project = projectService.createProject(
                request.name(), request.description(), creator.get());

        return ResponseEntity
                .created(URI.create("/api/projects/" + project.getId()))
                .body(ProjectResponse.from(project));
    }

    // PUT /api/projects/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable UUID id,
            @RequestBody UpdateProjectRequest request) {

        return projectService.findById(id)
                .map(project -> projectService.updateProject(
                        project,
                        request.name(),
                        request.description(),
                        request.status()))
                .map(ProjectResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/projects/{id}/members — add a member
    @PostMapping("/{id}/members")
    public ResponseEntity<ProjectResponse> addMember(
            @PathVariable UUID id,
            @RequestBody AddMemberRequest request) {

        var project = projectService.findById(id);
        var user    = userService.findByEmail(request.userEmail());

        if (project.isEmpty()) return ResponseEntity.notFound().build();
        if (user.isEmpty())    return ResponseEntity.badRequest().build();

        projectService.addMember(project.get(), user.get());
        return ResponseEntity.ok(ProjectResponse.from(project.get()));
    }

    // DELETE /api/projects/{id} → 204 No Content
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable UUID id) {
        if (projectService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
}