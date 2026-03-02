package com.collabhub.service;

import com.collabhub.domain.Project;
import com.collabhub.domain.User;
import com.collabhub.exception.ResourceNotFoundException;
import com.collabhub.repository.ProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProjectService {

    private static final Logger log = LoggerFactory.getLogger(ProjectService.class);

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Transactional
    @CacheEvict(value = "projects", allEntries = true)
    public Project createProject(String name, String description, User createdBy) {
        log.debug("Creating project '{}' for user '{}'", name, createdBy.getEmail());
        Project project = new Project(name, description);
        project.addMember(createdBy);
        Project saved = projectRepository.save(project);
        log.info("Project created: '{}' id={} by={}",
                name, saved.getId(), createdBy.getEmail());
        return saved;
    }

    @Transactional(readOnly = true)
    public Project getById(UUID id) {
        log.debug("Fetching project id={}", id);
        return projectRepository.findByIdWithMembers(id)
                .orElseThrow(() -> {
                    log.warn("Project not found: id={}", id);
                    return new ResourceNotFoundException("Project", id);
                });
    }

    @Transactional
    @CacheEvict(value = "projects", allEntries = true)
    public void addMember(Project project, User user) {
        log.debug("Adding member '{}' to project '{}'",
                user.getEmail(), project.getName());
        project.addMember(user);
        projectRepository.save(project);
        log.info("Member added: user={} project={}",
                user.getEmail(), project.getId());
    }

    @Transactional(readOnly = true)
    @Cacheable("projects")
    public List<Project> getAllProjects() {
        log.debug("Fetching all projects");
        return projectRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Project> getActiveProjects() {
        return projectRepository.findByStatus("ACTIVE");
    }

    @Transactional(readOnly = true)
    public List<Project> findByMember(User user) {
        log.debug("Finding projects for member '{}'", user.getEmail());
        return projectRepository.findByMember(user);
    }

    @Transactional
    @CacheEvict(value = "projects", allEntries = true)
    public Project updateProject(Project project, String name,
                                 String description, String status) {
        log.debug("Updating project id={}", project.getId());
        if (name        != null) project.setName(name);
        if (description != null) project.setDescription(description);
        if (status      != null) project.setStatus(status);
        Project saved = projectRepository.save(project);
        log.info("Project updated: id={} name='{}' status={}",
                saved.getId(), saved.getName(), saved.getStatus());
        return saved;
    }

    @Transactional
    @CacheEvict(value = "projects", allEntries = true)
    public void deleteProject(UUID id) {
        log.debug("Deleting project id={}", id);
        Project project = getById(id);
        projectRepository.delete(project);
        log.info("Project deleted: id={}", id);
    }
}