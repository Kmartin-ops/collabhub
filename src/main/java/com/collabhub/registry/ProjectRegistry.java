package com.collabhub.registry;

import com.collabhub.domain.Project;
import com.collabhub.domain.User;

import java.util.*;

public class ProjectRegistry {

    // UUID → Project for O(1) lookup
    private final Map<UUID, Project> store = new HashMap<>();

    public Project save(Project project) {
        store.put(project.getId(), project);
        return project;
    }

    public Optional<Project> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    public Collection<Project> findAll() {
        return store.values();
    }

    // Find all projects a specific user is a member of
    public List<Project> findByMember(User user) {
        return store.values().stream()
                .filter(p -> p.getMembers().contains(user)) // Set.contains() uses equals()
                .toList();
    }

    // Find active projects only
    public List<Project> findActive() {
        return store.values().stream()
                .filter(p -> "ACTIVE".equals(p.getStatus()))
                .toList();
    }

    public boolean exists(UUID id) {
        return store.containsKey(id);
    }

    public int count() {
        return store.size();
    }

    public void delete(UUID id) {
        store.remove(id);
    }
}