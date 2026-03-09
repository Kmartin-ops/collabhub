package com.collabhub.repository;

import com.collabhub.domain.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID>, JpaSpecificationExecutor<Task> {

    List<Task> findByProjectId(UUID projectId);

    List<Task> findByProjectIdAndStatus(UUID projectId, String status);

    List<Task> findByAssigneeId(UUID assigneeId);

    List<Task> findByDueDateBeforeAndStatusNot(LocalDate date, String status);

    List<Task> findByProjectIdAndPriority(UUID projectId, String priority);

    @Query("""
              SELECT t FROM Task t
              LEFT JOIN FETCH t.assignee
              WHERE t.project.id =:projectId
              ORDER BY t.dueDate ASC
            """)
    List<Task> findByProjectIdWithAssignee(@Param("projectId") UUID projectId);

    @Query("""
              SELECT t FROM Task t
              LEFT JOIN FETCH t.assignee
              LEFT JOIN FETCH t.project
              WHERE t.project.id =:projectId
              ORDER BY t.priority DESC, t.dueDate ASC
            """)
    List<Task> findKanbanBoard(@Param("projectId") UUID projectId);

    @Query("""
              SELECT t.status, COUNT(t)
              FROM Task t
              WHERE t.project.id =:projectId
              GROUP BY t.status
            """)
    List<Object[]> countByStatusForProject(@Param("projectId") UUID projectId);
}
