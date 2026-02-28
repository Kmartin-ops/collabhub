package com.collabhub.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Task extends BaseEntity implements Describable {

    private String title;
    private String status; // "BACKLOG", "IN_PROGRESS", "IN_REVIEW", "DONE"
    private String priority; // "LOW", "MEDIUM", "HIGH"
    private LocalDate dueDate;
    private User assignee;
    private Project project;
    private List<Comment> comments;

    public Task(String title, String priority, LocalDate dueDate, Project project) {
        super();
        this.title = title;
        this.priority = priority;
        this.dueDate = dueDate;
        this.project = project;
        this.status = "BACKLOG";
        this.comments = new ArrayList<>();
    }

    // Implementing the Describable contract
    @Override
    public String describe() {
        String assigneeName = (assignee != null) ? assignee.getName() : "Unassigned";
        return "Task: '" + title + "' | Priority: " + priority +
                " | Status: " + status + " | Assignee: " + assigneeName +
                " | Due: " + dueDate;
    }

    public void addComment(Comment comment) {
        comments.add(comment);
    }

    // Getters
    public String getTitle() { return title; }
    public String getStatus() { return status; }
    public String getPriority() { return priority; }
    public LocalDate getDueDate() { return dueDate; }
    public User getAssignee() { return assignee; }
    public Project getProject() { return project; }
    public List<Comment> getComments() { return comments; }

    // Setters
    public void setTitle(String title) { this.title = title; }
    public void setStatus(String status) { this.status = status; }
    public void setPriority(String priority) { this.priority = priority; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public void setAssignee(User assignee) { this.assignee = assignee; }

    @Override
    public String toString() {
        return "Task{" + super.toString() +
                ", title='" + title + '\'' +
                ", status='" + status + '\'' +
                ", priority='" + priority + '\'' + '}';
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task other)) return false;
        return getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}