package com.collabhub.domain;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "comments")
public class Comment extends BaseEntity {

    @Column(nullable = false, length = 1000)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    // Back-reference to the task this comment belongs to
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    protected Comment() {}

    public Comment(String content, User author) {
        super();
        this.content = content;
        this.author  = author;
    }

    public String getContent() { return content; }
    public User getAuthor()    { return author; }
    public Task getTask()      { return task; }

    public void setContent(String content) { this.content = content; }
    public void setTask(Task task)         { this.task = task; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Comment other)) return false;
        return Objects.equals(getId(), other.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        return "Comment{" + super.toString()
                + ", author='" + author.getName() + '\''
                + ", content='" + content + '\'' + '}';
    }
}