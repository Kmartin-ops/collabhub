package com.collabhub.service;

import com.collabhub.domain.Project;
import com.collabhub.domain.User;
import com.collabhub.exception.ResourceNotFoundException;
import com.collabhub.registry.ProjectRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
public class ProjectService {

    private static final Logger log = LoggerFactory.getLogger(ProjectService.class);

    private final ProjectRegistry registry = new ProjectRegistry();

    public Project createProject(String name, String description, User createdBy) {
        log.debug("Creating project '{}' for user '{}'", name, createdBy.getEmail());
        Project project = new Project(name, description);
        project.addMember(createdBy);
        registry.save(project);
        log.info("Project created: '{}' id={} by={}",
                name, project.getId(), createdBy.getEmail());
        return project;
    }

    public Project getById(UUID id) {
        log.debug("Fetching project id={}", id);
        return registry.findById(id)
                .orElseThrow(() -> {
                    log.warn("Project not found: id={}", id);
                    return new ResourceNotFoundException("Project", id);
                });
    }

    public void addMember(Project project, User user) {
        log.debug("Adding member '{}' to project '{}'",
                user.getEmail(), project.getName());
        project.addMember(user);
        registry.save(project);
        log.info("Member added: user={} project={}",
                user.getEmail(), project.getId());
    }

    public Collection<Project> getAllProjects() {
        log.debug("Fetching all projects, count={}", registry.count());
        return registry.findAll();
    }

    public List<Project> getActiveProjects() {
        return registry.findActive();
    }

    public List<Project> findByMember(User user) {
        log.debug("Finding projects for member '{}'", user.getEmail());
        return registry.findByMember(user);
    }

    public Project updateProject(Project project, String name,
                                 String description, String status) {
        log.debug("Updating project id={}", project.getId());
        if (name        != null) project.setName(name);
        if (description != null) project.setDescription(description);
        if (status      != null) project.setStatus(status);
        registry.save(project);
        log.info("Project updated: id={} name='{}' status={}",
                project.getId(), project.getName(), project.getStatus());
        return project;
    }

    public void deleteProject(UUID id) {
        log.debug("Deleting project id={}", id);
        getById(id);
        registry.delete(id);
        log.info("Project deleted: id={}", id);
    }
}