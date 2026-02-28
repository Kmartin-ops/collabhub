package com.collabhub;

import com.collabhub.domain.*;
import com.collabhub.notification.ConsoleNotification;
import com.collabhub.service.TaskService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TaskService")
class TaskServiceTest {

    private TaskService taskService;
    private Project project;
    private User alice;
    private User bob;

    @BeforeEach
    void setUp() {
        taskService = new TaskService(new ConsoleNotification());
        alice = new User("Alice", "alice@test.com", "MANAGER");
        bob   = new User("Bob",   "bob@test.com",   "DEVELOPER");
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
        @DisplayName("should assign a UUID to new task")
        void shouldAssignId() {
            Task task = taskService.createTask(
                    "Test task", "MEDIUM", LocalDate.now().plusDays(1), project, alice);

            assertNotNull(task.getId());
        }

        @Test
        @DisplayName("should assign a createdAt timestamp")
        void shouldAssignCreatedAt() {
            Task task = taskService.createTask(
                    "Test task", "LOW", LocalDate.now().plusDays(5), project, alice);

            assertNotNull(task.getCreatedAt());
        }

        @Test
        @DisplayName("should store task in registry")
        void shouldStoreInRegistry() {
            taskService.createTask("Task A", "HIGH", LocalDate.now().plusDays(2), project, alice);
            taskService.createTask("Task B", "LOW",  LocalDate.now().plusDays(4), project, alice);

            assertEquals(2, taskService.findByProject(project).size());
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
        @DisplayName("should find task under assignee's workload")
        void shouldAppearInAssigneeWorkload() {
            Task t1 = taskService.createTask("Task 1", "HIGH", LocalDate.now().plusDays(1), project, alice);
            Task t2 = taskService.createTask("Task 2", "LOW",  LocalDate.now().plusDays(2), project, alice);

            taskService.assignTask(t1, bob, alice);
            taskService.assignTask(t2, bob, alice);

            List<Task> bobsTasks = taskService.findByAssignee(bob);
            assertEquals(2, bobsTasks.size());
        }

        @Test
        @DisplayName("should allow reassigning task to different user")
        void shouldAllowReassignment() {
            Task task = taskService.createTask(
                    "Fix bug", "HIGH", LocalDate.now().plusDays(1), project, alice);

            taskService.assignTask(task, bob,   alice);
            taskService.assignTask(task, alice, bob);   // reassign

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
            taskService.createTask("Overdue task", "HIGH",
                    LocalDate.now().minusDays(2), project, alice); // past due

            taskService.createTask("Future task", "HIGH",
                    LocalDate.now().plusDays(5), project, alice);  // not due yet

            List<Task> overdue = taskService.findOverdue();
            assertEquals(1, overdue.size());
            assertEquals("Overdue task", overdue.get(0).getTitle());
        }

        @Test
        @DisplayName("should not include DONE tasks in overdue")
        void shouldExcludeDoneTasks() {
            Task task = taskService.createTask("Done overdue", "HIGH",
                    LocalDate.now().minusDays(1), project, alice);
            taskService.assignTask(task, bob, alice);
            taskService.changeStatus(task, "DONE", bob); // mark done

            List<Task> overdue = taskService.findOverdue();
            assertTrue(overdue.isEmpty());
        }
    }
}