package com.collabhub.domain;

public class User extends BaseEntity {

    private String name;
    private String email;
    private String role; // e.g. "DEVELOPER", "MANAGER", "ADMIN"

    public User(String name, String email, String role) {
        super(); // calls BaseEntity constructor — sets id + createdAt
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }

    public void setName(String name) { this.name = name; }
    public void setRole(String role) { this.role = role; }
    // No setEmail — email is an identity, shouldn't change

    @Override
    public String toString() {
        return "User{" + super.toString() +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' + '}';
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User other)) return false;
        return email.equals(other.email); // same email = same user
    }

    @Override
    public int hashCode() {
        return email.hashCode();
    }
}