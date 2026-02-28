package com.collabhub.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public abstract class BaseEntity {

    private UUID id;
    private LocalDateTime createdAt;

    // Constructor — every entity gets an ID and timestamp the moment it's created
    public BaseEntity() {
        this.id = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
    }

    // Getters only — id and createdAt should never change after creation
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