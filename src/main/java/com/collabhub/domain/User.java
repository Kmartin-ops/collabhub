package com.collabhub.domain;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String role;

    // JPA requires a no-arg constructor
    protected User() {}

    public User(String name, String email, String role) {
        super();
        this.name  = name;
        this.email = email;
        this.role  = role;
    }

    public String getName()  { return name; }
    public String getEmail() { return email; }
    public String getRole()  { return role; }

    public void setName(String name)   { this.name = name; }
    public void setRole(String role)   { this.role = role; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User other)) return false;
        return Objects.equals(email, other.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }

    @Override
    public String toString() {
        return "User{" + super.toString()
                + ", name='" + name + '\''
                + ", email='" + email + '\''
                + ", role='" + role + '\'' + '}';
    }
}