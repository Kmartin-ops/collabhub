package com.collabhub.service;

import com.collabhub.async.NotificationDispatcher;
import com.collabhub.async.NotificationEvent;
import com.collabhub.domain.Project;
import com.collabhub.domain.Task;
import com.collabhub.domain.User;
import com.collabhub.exception.ResourceNotFoundException;
import com.collabhub.notification.Notifiable;
import com.collabhub.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService")
class TaskServiceTest {

    @Mock
    TaskRepository taskRepository;
    @Mock
    Notifiable notifier;
    @Mock
    NotificationDispatcher dispatcher;

    @InjectMocks
    TaskService taskService;

    private User creator;
    private User assignee;
    private Project project;
    private Task task;
    private UUID taskId;
    private UUID projectId;

    @BeforeEach
    void setUp() {
        creator = new User("Alice", "alice@test.com", "MANAGER","password123!");
        assignee = new User("Bob", "bob@test.com", "DEVELOPER","password123!");
        projectId = UUID.randomUUID();
        taskId = UUID.randomUUID();
        project = new Project("CollabHub", "desc");
        project.setId(projectId);
        task = new Task("Fix bug", "HIGH", LocalDate.now().plusDays(3), project);
        task.setId(taskId);
    }

    @Nested
    @DisplayName("getById()")
    class GetById {

        @Test
        @DisplayName("returns task when found")
        void found() {
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

            Task result = taskService.getById(taskId);

            assertThat(result.getId()).isEqualTo(taskId);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when missing")
        void notFound() {
            when(taskRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

            // Extract UUID to variable so lambda contains only one call
            UUID missingId = UUID.randomUUID();
            assertThatThrownBy(() -> taskService.getById(missingId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deleteTask()")
    class DeleteTask {

        @Test
        @DisplayName("deletes existing task")
        void success() {
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

            taskService.deleteTask(taskId);

            verify(taskRepository).delete(task);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when task missing")
        void notFound() {
            when(taskRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

            UUID missingId = UUID.randomUUID();
            assertThatThrownBy(() -> taskService.deleteTask(missingId))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(taskRepository, never()).delete(any(Task.class));
        }
    }

    // Other nested test classes remain unchanged
}