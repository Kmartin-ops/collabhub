package com.collabhub.domain;

import java.util.HashSet;
import java.util.Set;

public class Project extends BaseEntity {

    private String name;
    private String description;
    private String status;
    private Set<User> members; // Changed from List to Set

    public Project(String name, String description) {
        super();
        this.name = name;
        this.description = description;
        this.status = "ACTIVE";
        this.members = new HashSet<>();
    }

    public void addMember(User user) {
        boolean added = members.add(user); // returns false if already present
        if (!added) {
            System.out.println("[Project] " + user.getName()
                    + " is already a member — skipped.");
        }
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public Set<User> getMembers() { return members; }

    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Project other)) return false;
        return getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public String toString() {
        return "Project{" + super.toString() +
                ", name='" + name + '\'' +
                ", status='" + status + '\'' +
                ", members=" + members.size() + '}';
    }
}