package com.collabhub.repository;

import com.collabhub.domain.Project;
import com.collabhub.domain.Task;
import com.collabhub.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("TaskRepository")
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TestEntityManager em;

    private User alice;
    private User bob;
    private Project project;
    private Task t1, t2, t3, t4;

    @BeforeEach
    void setUp() {
        alice   = em.persistAndFlush(new User("Alice", "alice@test.com", "MANAGER"));
        bob     = em.persistAndFlush(new User("Bob",   "bob@test.com",   "DEVELOPER"));
        project = em.persistAndFlush(new Project("Test Project", "desc"));

        t1 = new Task("Build API",      "HIGH",   LocalDate.now().plusDays(3),  project);
        t2 = new Task("Write tests",    "MEDIUM", LocalDate.now().plusDays(5),  project);
        t3 = new Task("Fix login bug",  "HIGH",   LocalDate.now().minusDays(1), project);
        t4 = new Task("Update docs",    "LOW",    LocalDate.now().plusDays(10), project);

        t1.setAssignee(bob);
        t2.setAssignee(bob);
        t3.setAssignee(alice);
        t3.setStatus("IN_PROGRESS");

        t1 = em.persistAndFlush(t1);
        t2 = em.persistAndFlush(t2);
        t3 = em.persistAndFlush(t3);
        t4 = em.persistAndFlush(t4);
    }

    @Nested
    @DisplayName("findByProjectId()")
    class FindByProjectId {

        @Test
        @DisplayName("should return all tasks for project")
        void shouldReturnAllTasksForProject() {
            List<Task> tasks = taskRepository.findByProjectId(project.getId());
            assertEquals(4, tasks.size());
        }

        @Test
        @DisplayName("should return empty for unknown project")
        void shouldReturnEmptyForUnknown() {
            List<Task> tasks = taskRepository.findByProjectId(
                    java.util.UUID.randomUUID());
            assertTrue(tasks.isEmpty());
        }
    }

    @Nested
    @DisplayName("findByProjectIdAndStatus()")
    class FindByStatus {

        @Test
        @DisplayName("should find tasks with matching status")
        void shouldFindByStatus() {
            List<Task> inProgress = taskRepository
                    .findByProjectIdAndStatus(project.getId(), "IN_PROGRESS");
            assertEquals(1, inProgress.size());
            assertEquals("Fix login bug", inProgress.get(0).getTitle());
        }

        @Test
        @DisplayName("should find backlog tasks")
        void shouldFindBacklogTasks() {
            List<Task> backlog = taskRepository
                    .findByProjectIdAndStatus(project.getId(), "BACKLOG");
            assertEquals(3, backlog.size());
        }
    }

    @Nested
    @DisplayName("findByAssigneeId()")
    class FindByAssignee {

        @Test
        @DisplayName("should find bob's tasks")
        void shouldFindBobsTasks() {
            List<Task> bobsTasks = taskRepository.findByAssigneeId(bob.getId());
            assertEquals(2, bobsTasks.size());
        }

        @Test
        @DisplayName("should find alice's tasks")
        void shouldFindAlicesTasks() {
            List<Task> alicesTasks = taskRepository.findByAssigneeId(alice.getId());
            assertEquals(1, alicesTasks.size());
            assertEquals("Fix login bug", alicesTasks.get(0).getTitle());
        }
    }

    @Nested
    @DisplayName("findByDueDateBeforeAndStatusNot()")
    class FindOverdue {

        @Test
        @DisplayName("should detect overdue task")
        void shouldDetectOverdueTask() {
            List<Task> overdue = taskRepository
                    .findByDueDateBeforeAndStatusNot(LocalDate.now(), "DONE");
            assertEquals(1, overdue.size());
            assertEquals("Fix login bug", overdue.get(0).getTitle());
        }

        @Test
        @DisplayName("should not include DONE tasks in overdue")
        void shouldExcludeDoneTasks() {
            t3.setStatus("DONE");
            em.persistAndFlush(t3);

            List<Task> overdue = taskRepository
                    .findByDueDateBeforeAndStatusNot(LocalDate.now(), "DONE");
            assertTrue(overdue.isEmpty());
        }
    }

    @Nested
    @DisplayName("Specification search")
    class SpecificationSearch {

        @Test
        @DisplayName("should filter by status using specification")
        void shouldFilterByStatus() {
            var spec = TaskSpecification.hasStatus("IN_PROGRESS");
            var pageable = PageRequest.of(0, 10);
            Page<Task> result = taskRepository.findAll(spec, pageable);

            assertEquals(1, result.getTotalElements());
        }

        @Test
        @DisplayName("should filter by priority using specification")
        void shouldFilterByPriority() {
            var spec = TaskSpecification.hasPriority("HIGH");
            var pageable = PageRequest.of(0, 10);
            Page<Task> result = taskRepository.findAll(spec, pageable);

            assertEquals(2, result.getTotalElements());
        }

        @Test
        @DisplayName("should filter by keyword in title")
        void shouldFilterByKeyword() {
            var spec = TaskSpecification.titleContains("login");
            var pageable = PageRequest.of(0, 10);
            Page<Task> result = taskRepository.findAll(spec, pageable);

            assertEquals(1, result.getTotalElements());
            assertEquals("Fix login bug", result.getContent().get(0).getTitle());
        }

        @Test
        @DisplayName("should combine multiple filters")
        void shouldCombineFilters() {
            var spec = TaskSpecification.withFilters(
                    null, "HIGH", project.getId(), null, null);
            var pageable = PageRequest.of(0, 10);
            Page<Task> result = taskRepository.findAll(spec, pageable);

            assertEquals(2, result.getTotalElements());
        }

        @Test
        @DisplayName("should paginate results correctly")
        void shouldPaginateCorrectly() {
            var spec     = TaskSpecification.withFilters(null, null, null, null, null);
            var pageable = PageRequest.of(0, 2, Sort.by("dueDate"));
            Page<Task> page0 = taskRepository.findAll(spec, pageable);

            assertEquals(4, page0.getTotalElements());
            assertEquals(2, page0.getTotalPages());
            assertEquals(2, page0.getContent().size());
            assertTrue(page0.isFirst());
            assertFalse(page0.isLast());
        }
    }
}