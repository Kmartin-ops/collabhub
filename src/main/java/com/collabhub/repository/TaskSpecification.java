/*
package com.collabhub.repository;

import com.collabhub.domain.Task;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

public class TaskSpecification {
    private TaskSpecification() {
    }

    public static Specification<Task> hasStatus(String status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Task> hasPriority(String priority) {
        return (root, query, cb) -> priority == null ? null : cb.equal(root.get("priority"), priority);
    }

    public static Specification<Task> hasProjectId(UUID projectId) {
        return (root, query, cb) -> projectId == null ? null : cb.equal(root.get("project").get("id"), projectId);
    }

    public static Specification<Task> hasAssigneeId(UUID assigneeId) {
        return (root, query, cb) -> assigneeId == null ? null : cb.equal(root.get("assignee").get("id"), assigneeId);
    }

    public static Specification<Task> isDueBefore(LocalDate date) {
        return (root, query, cb) -> date == null ? null : cb.lessThan(root.get("dueDate"), date);
    }

    public static Specification<Task> isDueAfter(LocalDate date) {
        return (root, query, cb) -> date == null ? null : cb.greaterThanOrEqualTo(root.get("dueDate"), date);
    }

    public static Specification<Task> titleContains(String keyword) {
        return (root, query, cb) -> keyword == null ? null
                : cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase() + "%");
    }

    // Combines all filters — null values are ignored automatically
    public static Specification<Task> withFilters(String status, String priority, UUID projectId, UUID assignedId,
            String keyword) {
        return Specification.where(hasStatus(status)).and(hasPriority(priority)).and(hasProjectId(projectId))
                .and(hasAssigneeId(assignedId)).and(titleContains(keyword));
    }

}
*/
package com.collabhub.repository;

import com.collabhub.domain.Task;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

public class TaskSpecification {
    private TaskSpecification() {
    }

    public static Specification<Task> hasStatus(String status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Task> hasPriority(String priority) {
        return (root, query, cb) -> priority == null ? null : cb.equal(root.get("priority"), priority);
    }

    public static Specification<Task> hasProjectId(UUID projectId) {
        return (root, query, cb) -> projectId == null ? null : cb.equal(root.get("project").get("id"), projectId);
    }

    public static Specification<Task> hasAssigneeId(UUID assigneeId) {
        return (root, query, cb) -> assigneeId == null ? null : cb.equal(root.get("assignee").get("id"), assigneeId);
    }

    public static Specification<Task> isDueBefore(LocalDate date) {
        return (root, query, cb) -> date == null ? null : cb.lessThan(root.get("dueDate"), date);
    }

    public static Specification<Task> isDueAfter(LocalDate date) {
        return (root, query, cb) -> date == null ? null : cb.greaterThanOrEqualTo(root.get("dueDate"), date);
    }

    public static Specification<Task> titleContains(String keyword) {
        return (root, query, cb) -> keyword == null ? null
                : cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase(Locale.ROOT) + "%");
    }

    // Combines all filters — null values are ignored automatically
    public static Specification<Task> withFilters(String status, String priority, UUID projectId, UUID assignedId,
                                                  String keyword) {
        return Specification.where(hasStatus(status))
                .and(hasPriority(priority))
                .and(hasProjectId(projectId))
                .and(hasAssigneeId(assignedId))
                .and(titleContains(keyword));
    }
}