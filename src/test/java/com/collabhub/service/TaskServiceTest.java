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
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
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
    @DisplayName("createTask()")
    class CreateTask {

        @Test
        @DisplayName("dispatches when dispatcher is set")
        void dispatchesWithDispatcher() {
            taskService.setDispatcher(dispatcher);
            when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
                Task saved = invocation.getArgument(0);
                saved.setId(taskId);
                return saved;
            });

            Task created = taskService.createTask("New task", "MEDIUM", LocalDate.now().plusDays(1), project, creator);

            assertThat(created.getId()).isEqualTo(taskId);
            verify(dispatcher).dispatch(any(NotificationEvent.class));
            verify(notifier, never()).notify(anyString(), anyString());
        }

        @Test
        @DisplayName("notifies directly when dispatcher is not set")
        void notifiesWhenNoDispatcher() {
            when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
                Task saved = invocation.getArgument(0);
                saved.setId(taskId);
                return saved;
            });

            taskService.createTask("New task", "MEDIUM", LocalDate.now().plusDays(1), project, creator);

            verify(notifier).notify(eq(creator.getName()),
                    contains("Task 'New task' was created in project: " + project.getName()));
            verify(dispatcher, never()).dispatch(any(NotificationEvent.class));
        }
    }

    @Nested
    @DisplayName("assignTask()")
    class AssignTask {

        @Test
        @DisplayName("notifies assignment when dispatcher is not set")
        void notifiesAssignment() {
            taskService.assignTask(task, assignee, creator);

            assertThat(task.getAssignee()).isEqualTo(assignee);
            verify(taskRepository).save(task);
            verify(notifier).notify(eq(creator.getName()),
                    contains("Task '" + task.getTitle() + "' was assigned to: " + assignee.getName()));
        }

        @Test
        @DisplayName("dispatches assignment when dispatcher is set")
        void dispatchesAssignment() {
            taskService.setDispatcher(dispatcher);

            taskService.assignTask(task, assignee, creator);

            verify(taskRepository).save(task);
            verify(dispatcher).dispatch(any(NotificationEvent.class));
            verify(notifier, never()).notify(anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("changeStatus()")
    class ChangeStatus {

        @Test
        @DisplayName("notifies on DONE status")
        void notifiesDone() {
            taskService.changeStatus(task, "DONE", creator);

            verify(taskRepository).save(task);
            verify(notifier).notify(eq(creator.getName()), contains("DONE"));
        }

        @Test
        @DisplayName("does not notify when status is not DONE")
        void doesNotNotifyForNonDone() {
            taskService.changeStatus(task, "IN_PROGRESS", creator);

            verify(taskRepository).save(task);
            verify(notifier, never()).notify(anyString(), anyString());
        }

        @Test
        @DisplayName("dispatches when dispatcher is set")
        void dispatchesWhenDispatcherSet() {
            taskService.setDispatcher(dispatcher);

            taskService.changeStatus(task, "DONE", creator);

            verify(dispatcher).dispatch(any(NotificationEvent.class));
            verify(notifier, never()).notify(anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("search()")
    class Search {

        @Test
        @DisplayName("delegates to repository with specification")
        void delegatesToRepository() {
            Page<Task> page = new PageImpl<>(List.of(task));
            when(taskRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(page);

            Page<Task> result = taskService.search("BACKLOG", "HIGH", projectId, null, "bug",
                    PageRequest.of(0, 10));

            assertThat(result.getContent()).hasSize(1);
            verify(taskRepository).findAll(any(Specification.class), any(PageRequest.class));
        }
    }

    @Nested
    @DisplayName("query helpers")
    class QueryHelpers {

        @Test
        @DisplayName("findByProject delegates to repository")
        void findByProject() {
            when(taskRepository.findByProjectIdWithAssignee(projectId)).thenReturn(List.of(task));

            List<Task> result = taskService.findByProject(projectId);

            assertThat(result).containsExactly(task);
        }

        @Test
        @DisplayName("findKanbanBoard delegates to repository")
        void findKanbanBoard() {
            when(taskRepository.findKanbanBoard(projectId)).thenReturn(List.of(task));

            List<Task> result = taskService.findKanbanBoard(projectId);

            assertThat(result).containsExactly(task);
        }

        @Test
        @DisplayName("findByAssignee delegates to repository")
        void findByAssignee() {
            UUID assigneeId = UUID.randomUUID();
            when(taskRepository.findByAssigneeId(assigneeId)).thenReturn(List.of(task));

            List<Task> result = taskService.findByAssignee(assigneeId);

            assertThat(result).containsExactly(task);
        }

        @Test
        @DisplayName("findOverdue delegates to repository")
        void findOverdue() {
            when(taskRepository.findByDueDateBeforeAndStatusNot(any(LocalDate.class), eq("DONE")))
                    .thenReturn(List.of(task));

            List<Task> result = taskService.findOverdue();

            assertThat(result).containsExactly(task);
        }

        @Test
        @DisplayName("findAll delegates to repository")
        void findAll() {
            when(taskRepository.findAll()).thenReturn(List.of(task));

            List<Task> result = taskService.findAll();

            assertThat(result).containsExactly(task);
        }
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
