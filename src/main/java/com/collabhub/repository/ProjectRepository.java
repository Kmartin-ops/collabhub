package com.collabhub.repository;

import com.collabhub.domain.Project;
import com.collabhub.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    // Derived — filter by status
    List<Project> findByStatus(String status);

    // JOIN FETCH members — prevents N+1 when we need member data
    @Query("""
        SELECT p FROM Project p
        LEFT JOIN FETCH p.members
        WHERE p.id = :id
        """)
    Optional<Project> findByIdWithMembers(@Param("id") UUID id);

    // Find all projects a user is a member of
    @Query("""
        SELECT p FROM Project p
        JOIN p.members m
        WHERE m = :user
        """)
    List<Project> findByMember(@Param("user") User user);

    // Find projects with their tasks loaded
    @Query("""
        SELECT DISTINCT p FROM Project p
        LEFT JOIN FETCH p.tasks
        WHERE p.id = :id
        """)
    Optional<Project> findByIdWithTasks(@Param("id") UUID id);

    // Count projects by status
    long countByStatus(String status);
}