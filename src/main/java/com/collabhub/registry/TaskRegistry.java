package com.collabhub.registry;

import com.collabhub.domain.Project;
import com.collabhub.domain.Task;
import com.collabhub.domain.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.UUID;

public class TaskRegistry {

    // UUID → Task for O(1) lookup by ID
    private final Map<UUID, Task> store = new HashMap<>();

    // Always-sorted by due date — soonest task is always at the head
    private final PriorityQueue<Task> priorityQueue = new PriorityQueue<>(Comparator.comparing(Task::getDueDate));

    public Task save(Task task) {
        store.put(task.getId(), task);
        priorityQueue.offer(task); // offer() adds to the queue in sorted order
        return task;
    }

    public Optional<Task> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    public Collection<Task> findAll() {
        return store.values();
    }

    // Find all tasks for a specific project
    public List<Task> findByProject(Project project) {
        return store.values().stream().filter(t -> t.getProject().getId().equals(project.getId())).toList();
    }

    // Find all tasks assigned to a specific user
    public List<Task> findByAssignee(User user) {
        return store.values().stream().filter(t -> t.getAssignee() != null && t.getAssignee().equals(user)).toList();
    }

    // Find all overdue tasks — due date is before today and not done
    public List<Task> findOverdue() {
        LocalDate today = LocalDate.now();
        return store.values().stream().filter(t -> t.getDueDate().isBefore(today))
                .filter(t -> !"DONE".equals(t.getStatus())).toList();
    }

    // Get the next most urgent task (soonest due date)
    // peek() reads the head without removing it
    public Optional<Task> getNextUrgent() {
        return Optional.ofNullable(priorityQueue.peek());
    }

    // Poll removes AND returns the head — use when actually processing a task
    public Optional<Task> pollNextUrgent() {
        return Optional.ofNullable(priorityQueue.poll());
    }

    // Get all tasks sorted by due date (snapshot of the priority queue)
    public List<Task> findAllSortedByDueDate() {
        return new ArrayList<>(priorityQueue); // copy so we don't drain the queue
    }

    public int count() {
        return store.size();
    }
}
