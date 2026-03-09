package com.collabhub.security;

import com.collabhub.domain.Project;
import com.collabhub.domain.Task;
import com.collabhub.domain.User;
import com.collabhub.repository.TaskRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskAuthService")
class TaskAuthServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskAuthService taskAuthService;

    @Test
    @DisplayName("isAssignee should return true for matching assignee email")
    void shouldReturnTrueWhenAssigneeMatches() {
        UUID taskId = UUID.randomUUID();
        User bob = new User("Bob", "bob@collabhub.com", "DEVELOPER","password123!");
        Project project = new Project("CollabHub", "Core");
        Task task = new Task("Implement API", "HIGH", LocalDate.now().plusDays(3), project);
        task.setAssignee(bob);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        assertThat(taskAuthService.isAssignee(taskId, "bob@collabhub.com")).isTrue();
    }

    @Test
    @DisplayName("isAssignee should return false when task has no assignee")
    void shouldReturnFalseWhenNoAssignee() {
        UUID taskId = UUID.randomUUID();
        Project project = new Project("CollabHub", "Core");
        Task task = new Task("Implement API", "HIGH", LocalDate.now().plusDays(3), project);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        assertThat(taskAuthService.isAssignee(taskId, "bob@collabhub.com")).isFalse();
    }

    @Test
    @DisplayName("isAssignee should return false when task is missing")
    void shouldReturnFalseWhenTaskMissing() {
        UUID taskId = UUID.randomUUID();
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThat(taskAuthService.isAssignee(taskId, "bob@collabhub.com")).isFalse();
    }
}
