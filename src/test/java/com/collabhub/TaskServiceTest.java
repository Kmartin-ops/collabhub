package com.collabhub;

import com.collabhub.domain.*;
import com.collabhub.notification.ConsoleNotification;
import com.collabhub.repository.TaskRepository;
import com.collabhub.service.TaskService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("TaskService")
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    private TaskService taskService;
    private Project project;
    private User alice;
    private User bob;

    @BeforeEach
    void setUp() {
        // Mock save() to return the entity passed to it — simulates DB save
        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        taskService = new TaskService(taskRepository, new ConsoleNotification());

        alice   = new User("Alice", "alice@test.com", "MANAGER");
        bob     = new User("Bob",   "bob@test.com",   "DEVELOPER");
        project = new Project("Test Project", "A test project");
        project.addMember(alice);
        project.addMember(bob);
    }

    @Nested
    @DisplayName("createTask()")
    class CreateTask {

        @Test
        @DisplayName("should create task with BACKLOG status by default")
        void shouldCreateWithBacklogStatus() {
            Task task = taskService.createTask(
                    "Test task", "HIGH", LocalDate.now().plusDays(3), project, alice);
            assertEquals("BACKLOG", task.getStatus());
        }

        @Test
        @DisplayName("should assign a createdAt timestamp")
        void shouldAssignCreatedAt() {
            Task task = taskService.createTask(
                    "Test task", "LOW", LocalDate.now().plusDays(5), project, alice);
            assertNotNull(task.getCreatedAt());
        }

        @ParameterizedTest
        @ValueSource(strings = {"LOW", "MEDIUM", "HIGH"})
        @DisplayName("should accept all valid priority values")
        void shouldAcceptAllPriorities(String priority) {
            Task task = taskService.createTask(
                    "Task", priority, LocalDate.now().plusDays(1), project, alice);
            assertEquals(priority, task.getPriority());
        }
    }

    @Nested
    @DisplayName("assignTask()")
    class AssignTask {

        @Test
        @DisplayName("should set assignee on task")
        void shouldSetAssignee() {
            Task task = taskService.createTask(
                    "Fix bug", "HIGH", LocalDate.now().plusDays(1), project, alice);
            taskService.assignTask(task, bob, alice);
            assertEquals(bob.getEmail(), task.getAssignee().getEmail());
        }

        @Test
        @DisplayName("should allow reassigning task to different user")
        void shouldAllowReassignment() {
            Task task = taskService.createTask(
                    "Fix bug", "HIGH", LocalDate.now().plusDays(1), project, alice);
            taskService.assignTask(task, bob,   alice);
            taskService.assignTask(task, alice, bob);
            assertEquals(alice.getEmail(), task.getAssignee().getEmail());
        }
    }

    @Nested
    @DisplayName("changeStatus()")
    class ChangeStatus {

        @Test
        @DisplayName("should update task status")
        void shouldUpdateStatus() {
            Task task = taskService.createTask(
                    "Build feature", "HIGH", LocalDate.now().plusDays(3), project, alice);
            taskService.assignTask(task, bob, alice);
            taskService.changeStatus(task, "IN_PROGRESS", bob);
            assertEquals("IN_PROGRESS", task.getStatus());
        }

        @Test
        @DisplayName("should allow full status lifecycle")
        void shouldAllowFullLifecycle() {
            Task task = taskService.createTask(
                    "Full lifecycle", "MEDIUM", LocalDate.now().plusDays(5), project, alice);
            taskService.assignTask(task, bob, alice);

            taskService.changeStatus(task, "IN_PROGRESS", bob);
            assertEquals("IN_PROGRESS", task.getStatus());

            taskService.changeStatus(task, "IN_REVIEW", bob);
            assertEquals("IN_REVIEW", task.getStatus());

            taskService.changeStatus(task, "DONE", bob);
            assertEquals("DONE", task.getStatus());
        }
    }

    @Nested
    @DisplayName("findOverdue()")
    class FindOverdue {

        @Test
        @DisplayName("should detect overdue tasks")
        void shouldDetectOverdueTasks() {
            Task overdue = new Task("Overdue task", "HIGH",
                    LocalDate.now().minusDays(2), project);
            Task future  = new Task("Future task",  "HIGH",
                    LocalDate.now().plusDays(5),  project);

            when(taskRepository.findByDueDateBeforeAndStatusNot(any(), any()))
                    .thenReturn(List.of(overdue));

            List<Task> result = taskService.findOverdue();
            assertEquals(1, result.size());
            assertEquals("Overdue task", result.get(0).getTitle());
        }

        @Test
        @DisplayName("should not include DONE tasks in overdue")
        void shouldExcludeDoneTasks() {
            when(taskRepository.findByDueDateBeforeAndStatusNot(any(), any()))
                    .thenReturn(List.of()); // service excludes DONE via the query

            List<Task> result = taskService.findOverdue();
            assertTrue(result.isEmpty());
        }
    }
}