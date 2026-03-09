package com.collabhub.repository;

import com.collabhub.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    // All comments for a task, ordered by creation time
    List<Comment> findByTaskIdOrderByCreatedAtAsc(UUID taskId);

    // All comments by a specific user
    List<Comment> findByAuthorId(UUID authorId);

    // Count comments per task
    long countByTaskId(UUID taskId);

    // Recent comments across a whole project
    @Query("""
            SELECT c FROM Comment c
            JOIN FETCH c.author
            WHERE c.task.project.id = :projectId
            ORDER BY c.createdAt DESC
            """)
    List<Comment> findRecentByProjectId(@Param("projectId") UUID projectId);
}
