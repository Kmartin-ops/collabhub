package com.collabhub.repository;

import com.collabhub.domain.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    // Derived — all tasks for a project
    List<Task> findByProjectId(UUID projectId);

    // Derived — all tasks for a project with a specific status
    List<Task> findByProjectIdAndStatus(UUID projectId, String status);

    // Derived — all tasks assigned to a specific user
    List<Task> findByAssigneeId(UUID assigneeId);

    // Derived — overdue tasks (due before today, not done)
    List<Task> findByDueDateBeforeAndStatusNot(LocalDate date, String status);

    // Derived — tasks by priority
    List<Task> findByProjectIdAndPriority(UUID projectId, String priority);

    // JOIN FETCH assignee — prevents N+1 when listing tasks with assignee info
    @Query("""
        SELECT t FROM Task t
        LEFT JOIN FETCH t.assignee
        WHERE t.project.id = :projectId
        ORDER BY t.dueDate ASC
        """)
    List<Task> findByProjectIdWithAssignee(@Param("projectId") UUID projectId);

    // JOIN FETCH everything needed for Kanban board display
    @Query("""
        SELECT t FROM Task t
        LEFT JOIN FETCH t.assignee
        LEFT JOIN FETCH t.project
        WHERE t.project.id = :projectId
        ORDER BY t.priority DESC, t.dueDate ASC
        """)
    List<Task> findKanbanBoard(@Param("projectId") UUID projectId);

    // Count tasks by status for a project — used in reporting
    @Query("""
        SELECT t.status, COUNT(t)
        FROM Task t
        WHERE t.project.id = :projectId
        GROUP BY t.status
        """)
    List<Object[]> countByStatusForProject(@Param("projectId") UUID projectId);
}