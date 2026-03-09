package com.collabhub;

import com.collabhub.domain.*;
import com.collabhub.report.PagedResult;
import com.collabhub.report.ReportEngine;
import com.collabhub.report.TaskSummary;
import com.collabhub.repository.TaskRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportEngine")
class ReportEngineTest {

    @Mock
    private TaskRepository taskRepository;

    private ReportEngine reportEngine;
    private Project project;
    private User alice;
    private User bob;
    private User carol;

    private Task t1, t2, t3, t4, t5;

    @BeforeEach
    void setUp() {
        alice = new User("Alice", "alice@test.com", "MANAGER","password123!");
        bob = new User("Bob", "bob@test.com", "DEVELOPER","password123!");
        carol = new User("Carol", "carol@test.com", "DEVELOPER","password123!");
        project = new Project("Test", "desc");

        t1 = new Task("Task 1", "HIGH", LocalDate.now().plusDays(1), project);
        t2 = new Task("Task 2", "MEDIUM", LocalDate.now().plusDays(3), project);
        t3 = new Task("Task 3", "HIGH", LocalDate.now().minusDays(1), project);
        t4 = new Task("Task 4", "LOW", LocalDate.now().plusDays(5), project);
        t5 = new Task("Task 5", "MEDIUM", LocalDate.now().plusDays(2), project);

        t1.setAssignee(bob);
        t2.setAssignee(bob);
        t3.setAssignee(bob); // overdue, bob's
        t4.setAssignee(carol);
        t5.setAssignee(carol);

        t2.setStatus("DONE");
        t5.setStatus("IN_PROGRESS");

        // Tell the mock to return our controlled task list
        when(taskRepository.findAll()).thenReturn(List.of(t1, t2, t3, t4, t5));

        reportEngine = new ReportEngine(taskRepository);
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

        assertEquals(1, partitioned.get(true).size());
        assertEquals(3, partitioned.get(false).size());
    }

    @Test
    @DisplayName("topUrgentTasks() should return correct number sorted by due date")
    void shouldReturnTopUrgentTasks() {
        List<Task> urgent = reportEngine.topUrgentTasks(2);

        assertEquals(2, urgent.size());
        assertTrue(urgent.get(0).getDueDate().isBefore(urgent.get(1).getDueDate())
                || urgent.get(0).getDueDate().isEqual(urgent.get(1).getDueDate()));
    }

    @Test
    @DisplayName("generateAssigneeSummary() should produce correct per-user stats")
    void shouldGenerateAssigneeSummary() {
        List<TaskSummary> summaries = reportEngine.generateAssigneeSummary();

        TaskSummary bobSummary = summaries.stream().filter(s -> s.assigneeName().equals("Bob")).findFirst()
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
            assertEquals(2, page.getContent().size());
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
