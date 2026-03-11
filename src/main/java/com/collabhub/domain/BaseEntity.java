package com.collabhub.domain;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import java.util.UUID;

@MappedSuperclass // not an entity itself — shares mappings with subclasses
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    // Only needed for tests — allows controlled ID assignment
    public void setId(UUID id) {
        this.id = id;
    }

    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    protected BaseEntity() {
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "id=" + id + ", createdAt=" + createdAt;
    }
}
