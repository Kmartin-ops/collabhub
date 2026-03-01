package com.collabhub.service;

import com.collabhub.domain.Project;
import com.collabhub.domain.User;
import com.collabhub.registry.ProjectRegistry;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProjectService {

    private final ProjectRegistry registry = new ProjectRegistry();

    public Project createProject(String name, String description, User createdBy) {
        Project project = new Project(name, description);
        project.addMember(createdBy);
        registry.save(project);
        System.out.println("[ProjectService] Created: '" + name
                + "' by " + createdBy.getName());
        return project;
    }

    public void addMember(Project project, User user) {
        project.addMember(user);
        registry.save(project);
    }

    public Optional<Project> findById(UUID id) {
        return registry.findById(id);
    }

    public List<Project> findByMember(User user) {
        return registry.findByMember(user);
    }

    public Collection<Project> getAllProjects() {
        return registry.findAll();
    }

    public List<Project> getActiveProjects() {
        return registry.findActive();
    }

    public Project updateProject(Project project, String name,
                                 String description, String status) {
        if (name        != null) project.setName(name);
        if (description != null) project.setDescription(description);
        if (status      != null) project.setStatus(status);
        registry.save(project);
        return project;
    }

    public void deleteProject(UUID id) {
        registry.delete(id);
    }
}