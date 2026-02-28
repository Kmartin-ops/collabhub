package com.collabhub;

import com.collabhub.domain.*;
import com.collabhub.notification.ConsoleNotification;
import com.collabhub.report.PagedResult;
import com.collabhub.report.ReportEngine;
import com.collabhub.report.TaskSummary;
import com.collabhub.service.TaskService;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ReportEngine")
class ReportEngineTest {

    private TaskService taskService;
    private ReportEngine reportEngine;
    private Project project;
    private User alice;
    private User bob;
    private User carol;

    @BeforeEach
    void setUp() {
        taskService = new TaskService(new ConsoleNotification());
        alice = new User("Alice", "alice@test.com", "MANAGER");
        bob   = new User("Bob",   "bob@test.com",   "DEVELOPER");
        carol = new User("Carol", "carol@test.com", "DEVELOPER");
        project = new Project("Test", "desc");

        // Seed a controlled set of tasks
        Task t1 = taskService.createTask("Task 1", "HIGH",   LocalDate.now().plusDays(1),  project, alice);
        Task t2 = taskService.createTask("Task 2", "MEDIUM", LocalDate.now().plusDays(3),  project, alice);
        Task t3 = taskService.createTask("Task 3", "HIGH",   LocalDate.now().minusDays(1), project, alice);
        Task t4 = taskService.createTask("Task 4", "LOW",    LocalDate.now().plusDays(5),  project, alice);
        Task t5 = taskService.createTask("Task 5", "MEDIUM", LocalDate.now().plusDays(2),  project, alice);

        taskService.assignTask(t1, bob,   alice);
        taskService.assignTask(t2, bob,   alice);
        taskService.assignTask(t3, bob,   alice); // overdue, bob's
        taskService.assignTask(t4, carol, alice);
        taskService.assignTask(t5, carol, alice);

        taskService.changeStatus(t2, "DONE", bob);
        taskService.changeStatus(t5, "IN_PROGRESS", carol);

        reportEngine = new ReportEngine(taskService.getRegistry());
    }

    @Test
    @DisplayName("countByStatus() should return correct counts")
    void shouldCountByStatus() {
        Map<String, Long> counts = reportEngine.countByStatus();

        assertEquals(1, counts.get("DONE"));
        assertEquals(1, counts.get("IN_PROGRESS"));
        assertEquals(3, counts.get("BACKLOG"));
    }

    @Test
    @DisplayName("countByPriority() should return correct counts")
    void shouldCountByPriority() {
        Map<String, Long> counts = reportEngine.countByPriority();

        assertEquals(2, counts.get("HIGH"));
        assertEquals(2, counts.get("MEDIUM"));
        assertEquals(1, counts.get("LOW"));
    }

    @Test
    @DisplayName("partitionByOverdue() should separate overdue from on-track")
    void shouldPartitionOverdue() {
        Map<Boolean, List<Task>> partitioned = reportEngine.partitionByOverdue();

        // t3 is overdue and not DONE
        assertEquals(1, partitioned.get(true).size());
        // t1, t4 are on-track (t2 is DONE excluded, t5 IN_PROGRESS on-track)
        assertEquals(3, partitioned.get(false).size());
    }

    @Test
    @DisplayName("topUrgentTasks() should return correct number sorted by due date")
    void shouldReturnTopUrgentTasks() {
        List<Task> urgent = reportEngine.topUrgentTasks(2);

        assertEquals(2, urgent.size());
        // First should be the earliest due date (t3 is overdue — most urgent)
        assertTrue(urgent.get(0).getDueDate()
                .isBefore(urgent.get(1).getDueDate())
                || urgent.get(0).getDueDate()
                .isEqual(urgent.get(1).getDueDate()));
    }

    @Test
    @DisplayName("generateAssigneeSummary() should produce correct per-user stats")
    void shouldGenerateAssigneeSummary() {
        List<TaskSummary> summaries = reportEngine.generateAssigneeSummary();

        TaskSummary bobSummary = summaries.stream()
                .filter(s -> s.assigneeName().equals("Bob"))
                .findFirst()
                .orElseThrow();

        assertEquals(3, bobSummary.totalTasks());
        assertEquals(1, bobSummary.overdueCount());
        assertEquals(1, bobSummary.doneCount());
    }

    @Nested
    @DisplayName("getTasksPaged()")
    class Pagination {

        @Test
        @DisplayName("should return correct page size")
        void shouldReturnCorrectPageSize() {
            PagedResult<Task> page = reportEngine.getTasksPaged(0, 3);
            assertEquals(3, page.getContent().size());
        }

        @Test
        @DisplayName("should return remaining items on last page")
        void shouldReturnRemainingOnLastPage() {
            PagedResult<Task> page = reportEngine.getTasksPaged(1, 3);
            assertEquals(2, page.getContent().size()); // 5 tasks, page 1 has 2
        }

        @Test
        @DisplayName("should report correct total elements")
        void shouldReportTotalElements() {
            PagedResult<Task> page = reportEngine.getTasksPaged(0, 10);
            assertEquals(5, page.getTotalElements());
        }

        @Test
        @DisplayName("should return empty page beyond data range")
        void shouldReturnEmptyBeyondRange() {
            PagedResult<Task> page = reportEngine.getTasksPaged(99, 10);
            assertTrue(page.getContent().isEmpty());
        }
    }
}