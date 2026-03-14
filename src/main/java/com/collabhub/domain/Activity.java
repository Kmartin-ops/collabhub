package com.collabhub.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "activities")
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String action;          // TASK_CREATED, STATUS_CHANGED, TASK_ASSIGNED, FILE_UPLOADED

    @Column(nullable = false)
    private String actorName;

    private String entityType;      // "TASK", "PROJECT"
    private UUID   entityId;
    private String entityName;

    private String detail;          // "BACKLOG → IN_PROGRESS"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Activity() {}

    public Activity(String action, String actorName, String entityType,
                    UUID entityId, String entityName, String detail, Project project) {
        this.action     = action;
        this.actorName  = actorName;
        this.entityType = entityType;
        this.entityId   = entityId;
        this.entityName = entityName;
        this.detail     = detail;
        this.project    = project;
    }

    public UUID          getId()         { return id; }
    public String        getAction()     { return action; }
    public String        getActorName()  { return actorName; }
    public String        getEntityType() { return entityType; }
    public UUID          getEntityId()   { return entityId; }
    public String        getEntityName() { return entityName; }
    public String        getDetail()     { return detail; }
    public Project       getProject()    { return project; }
    public LocalDateTime getCreatedAt()  { return createdAt; }
}
