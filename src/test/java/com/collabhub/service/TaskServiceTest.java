package com.collabhub.service;

import com.collabhub.async.NotificationDispatcher;
import com.collabhub.async.NotificationEvent;
import com.collabhub.domain.Project;
import com.collabhub.domain.Task;
import com.collabhub.domain.User;
import com.collabhub.exception.ResourceNotFoundException;
import com.collabhub.notification.Notifiable;
import com.collabhub.repository.TaskRepository;
import com.collabhub.repository.TaskSpecification;
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

    // ── createTask ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("createTask()")
    class CreateTask {

        @Test
        @DisplayName("saves and returns task, dispatches event")
        void withDispatcher() {
            taskService.setDispatcher(dispatcher);
            when(taskRepository.save(any(Task.class))).thenReturn(task);

            Task result = taskService.createTask("Fix bug", "HIGH", LocalDate.now().plusDays(3), project, creator);

            assertThat(result.getTitle()).isEqualTo("Fix bug");
            verify(taskRepository).save(any(Task.class));
            verify(dispatcher).dispatch(any(NotificationEvent.class));
        }

        @Test
        @DisplayName("falls back to notifier when no dispatcher")
        void withoutDispatcher() {
            when(taskRepository.save(any(Task.class))).thenReturn(task);

            // dispatcher is null (not set) — handler.handle() should be called instead
            Task result = taskService.createTask("Fix bug", "HIGH", LocalDate.now().plusDays(3), project, creator);

            assertThat(result).isNotNull();
            verify(dispatcher, never()).dispatch(any());
        }
    }

    // ── assignTask ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("assignTask()")
    class AssignTask {

        @Test
        @DisplayName("sets assignee, saves, and dispatches event")
        void withDispatcher() {
            taskService.setDispatcher(dispatcher);
            when(taskRepository.save(task)).thenReturn(task);

            taskService.assignTask(task, assignee, creator);

            assertThat(task.getAssignee()).isEqualTo(assignee);
            verify(taskRepository).save(task);
            verify(dispatcher).dispatch(any(NotificationEvent.class));
        }

        @Test
        @DisplayName("falls back to notifier when no dispatcher")
        void withoutDispatcher() {
            when(taskRepository.save(task)).thenReturn(task);

            taskService.assignTask(task, assignee, creator);

            assertThat(task.getAssignee()).isEqualTo(assignee);
            verify(dispatcher, never()).dispatch(any());
        }
    }

    // ── changeStatus ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("changeStatus()")
    class ChangeStatus {

        @Test
        @DisplayName("updates status and saves")
        void updatesStatus() {
            when(taskRepository.save(task)).thenReturn(task);

            taskService.changeStatus(task, "IN_PROGRESS", creator);

            assertThat(task.getStatus()).isEqualTo("IN_PROGRESS");
            verify(taskRepository).save(task);
        }

        @Test
        @DisplayName("dispatches TASK_COMPLETED event when status is DONE")
        void dispatchesOnDone() {
            taskService.setDispatcher(dispatcher);
            when(taskRepository.save(task)).thenReturn(task);

            taskService.changeStatus(task, "DONE", creator);

            verify(dispatcher).dispatch(any(NotificationEvent.class));
        }

        @Test
        @DisplayName("does not dispatch event for non-DONE status")
        void noDispatchForOtherStatus() {
            taskService.setDispatcher(dispatcher);
            when(taskRepository.save(task)).thenReturn(task);

            taskService.changeStatus(task, "IN_PROGRESS", creator);

            verify(dispatcher, never()).dispatch(any());
        }
    }

    // ── getById ───────────────────────────────────────────────────────────────

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

            assertThatThrownBy(() -> taskService.getById(UUID.randomUUID()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ── search ────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("search()")
    class Search {

        @Test
        @DisplayName("delegates to repository with spec and pageable")
        void success() {
            var pageable = PageRequest.of(0, 10);
            Page<Task> page = new PageImpl<>(List.of(task));
            when(taskRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

            Page<Task> result = taskService.search("TODO", "HIGH", projectId, null, null, pageable);

            assertThat(result.getContent()).containsExactly(task);
        }
    }

    // ── findByProject ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("findByProject()")
    class FindByProject {

        @Test
        @DisplayName("returns tasks for given project")
        void success() {
            when(taskRepository.findByProjectIdWithAssignee(projectId)).thenReturn(List.of(task));

            List<Task> results = taskService.findByProject(projectId);

            assertThat(results).containsExactly(task);
        }
    }

    // ── findOverdue ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("findOverdue()")
    class FindOverdue {

        @Test
        @DisplayName("queries for tasks due before today that are not DONE")
        void success() {
            when(taskRepository.findByDueDateBeforeAndStatusNot(any(LocalDate.class), eq("DONE")))
                    .thenReturn(List.of(task));

            List<Task> results = taskService.findOverdue();

            assertThat(results).containsExactly(task);
        }
    }

    // ── findAll ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("findAll()")
    class FindAll {

        @Test
        @DisplayName("returns all tasks")
        void success() {
            when(taskRepository.findAll()).thenReturn(List.of(task));

            assertThat(taskService.findAll()).containsExactly(task);
        }
    }

    // ── findKanbanBoard ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("findKanbanBoard()")
    class FindKanbanBoard {

        @Test
        @DisplayName("returns kanban board tasks for project")
        void success() {
            when(taskRepository.findKanbanBoard(projectId)).thenReturn(List.of(task));

            List<Task> results = taskService.findKanbanBoard(projectId);

            assertThat(results).containsExactly(task);
        }
    }

    // ── findByAssignee ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("findByAssignee()")
    class FindByAssignee {

        @Test
        @DisplayName("returns tasks assigned to user")
        void success() {
            UUID assigneeId = UUID.randomUUID();
            when(taskRepository.findByAssigneeId(assigneeId)).thenReturn(List.of(task));

            List<Task> results = taskService.findByAssignee(assigneeId);

            assertThat(results).containsExactly(task);
        }
    }

    // ── deleteTask ────────────────────────────────────────────────────────────

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

            assertThatThrownBy(() -> taskService.deleteTask(UUID.randomUUID()))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(taskRepository, never()).delete(any(Task.class));
        }
    }
}
