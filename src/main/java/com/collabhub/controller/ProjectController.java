package com.collabhub.controller;

import com.collabhub.dto.CreateProjectRequest;
import com.collabhub.dto.ProjectResponse;
import com.collabhub.dto.UpdateProjectRequest;
import com.collabhub.mapper.ProjectMapper;
import com.collabhub.service.ProjectService;
import com.collabhub.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
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
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
@Tag(name = "Projects", description = "Project management endpoints")
public class ProjectController {

    private final ProjectService projectService;
    private final UserService userService;
    private final ProjectMapper projectMapper;

    public ProjectController(ProjectService projectService, UserService userService, ProjectMapper projectMapper) {
        this.projectService = projectService;
        this.userService = userService;
        this.projectMapper = projectMapper;
    }

    @GetMapping
    @Operation(summary = "Get all projects")
    public List<ProjectResponse> getAllProjects(@RequestParam(required = false) String status,
            Authentication authentication) {
        List<com.collabhub.domain.Project> projects;
        if (authentication != null && hasRole(authentication, "ROLE_DEVELOPER") && !hasRole(authentication, "ROLE_MANAGER")) {
            var user = userService.getByEmail(authentication.getName());
            projects = projectService.findByMember(user);
        } else {
            projects = projectService.getAllProjects();
        }
        return projects.stream()
                .filter(project -> status == null || status.isBlank() || status.equalsIgnoreCase(project.getStatus()))
                .map(projectMapper::toResponse).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get project by ID")
    public ProjectResponse getById(@PathVariable UUID id) {
        return projectMapper.toResponse(projectService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')") // ← MANAGER only
    @Operation(summary = "Create a new project")
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody CreateProjectRequest request,
            Principal principal, HttpServletRequest httpRequest) {
        var creatorEmail = resolveAuthenticatedEmail(principal, httpRequest);
        var creator = userService.getByEmail(creatorEmail);
        var project = projectService.createProject(request.name(), request.description(), creator);
        return ResponseEntity.created(URI.create("/api/projects/" + project.getId()))
                .body(projectMapper.toResponse(project));
    }

    private String resolveAuthenticatedEmail(Principal principal, HttpServletRequest request) {
        if (principal != null && principal.getName() != null) {
            return principal.getName();
        }
        Principal requestPrincipal = request.getUserPrincipal();
        if (requestPrincipal != null && requestPrincipal.getName() != null) {
            return requestPrincipal.getName();
        }
        Object securityContext = request.getSession(false) == null ? null
                : request.getSession(false).getAttribute("SPRING_SECURITY_CONTEXT");
        if (securityContext instanceof SecurityContext context && context.getAuthentication() != null
                && context.getAuthentication().getName() != null) {
            return context.getAuthentication().getName();
        }
        throw new IllegalStateException("Authenticated user is required to create a project");
    }

    private boolean hasRole(Authentication authentication, String role) {
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (role.equals(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    @PostMapping("/{id}/members")
    @PreAuthorize("hasRole('MANAGER')") // ← MANAGER only
    @Operation(summary = "Add a member to a project")
    public ProjectResponse addMember(@PathVariable UUID id, @RequestParam String email) {
        var project = projectService.getById(id);
        var user = userService.getByEmail(email);
        projectService.addMember(project, user);
        return projectMapper.toResponse(projectService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')") // ← MANAGER only
    @Operation(summary = "Update a project")
    public ProjectResponse updateProject(@PathVariable UUID id, @Valid @RequestBody UpdateProjectRequest request) {
        var project = projectService.getById(id);
        projectService.updateProject(project, request.name(), request.description(), request.status());
        return projectMapper.toResponse(projectService.getById(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')") // ← MANAGER only
    @Operation(summary = "Delete a project")
    public ResponseEntity<Void> deleteProject(@PathVariable UUID id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
}
