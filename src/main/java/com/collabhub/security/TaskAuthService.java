package com.collabhub.security;

import com.collabhub.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("taskAuthService")
public class TaskAuthService {

    private final TaskRepository taskRepository;

    public TaskAuthService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    // Returns true if the given email is the assignee of the task
    public boolean isAssignee(UUID taskId, String email) {
        return taskRepository.findById(taskId)
                .map(task -> task.getAssignee() != null && task.getAssignee().getEmail().equals(email)).orElse(false);
    }
}
