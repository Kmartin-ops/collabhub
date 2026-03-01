package com.collabhub.domain;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "projects")
public class Project extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false)
    private String status;

    // Many projects ↔ many users
    // LAZY — don't load members unless explicitly accessed
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "project_members",
            joinColumns        = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> members = new HashSet<>();

    protected Project() {}

    public Project(String name, String description) {
        super();
        this.name        = name;
        this.description = description;
        this.status      = "ACTIVE";
    }

    public void addMember(User user) {
        boolean added = members.add(user);
        if (!added) {
            System.out.println("[Project] " + user.getName()
                    + " is already a member — skipped.");
        }
    }

    public void removeMember(User user) {
        members.remove(user);
    }

    public String getName()        { return name; }
    public String getDescription() { return description; }
    public String getStatus()      { return status; }
    public Set<User> getMembers()  { return members; }

    public void setName(String name)               { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setStatus(String status)           { this.status = status; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Project other)) return false;
        return Objects.equals(getId(), other.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        return "Project{" + super.toString()
                + ", name='" + name + '\''
                + ", status='" + status + '\''
                + ", members=" + members.size() + '}';
    }
}