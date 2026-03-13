package com.collabhub.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "attachments")
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false)
    private String storedFileName;

    @Column(nullable = false)
    private String contentType;

    private long fileSize;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by")
    private User uploadedBy;

    @Column(nullable = false)
    private LocalDateTime uploadedAt = LocalDateTime.now();

    public Attachment() {}

    public Attachment(String originalFileName, String storedFileName, String contentType,
                      long fileSize, Task task, User uploadedBy) {
        this.originalFileName = originalFileName;
        this.storedFileName   = storedFileName;
        this.contentType      = contentType;
        this.fileSize         = fileSize;
        this.task             = task;
        this.uploadedBy       = uploadedBy;
    }

    public UUID getId()                  { return id; }
    public String getOriginalFileName()  { return originalFileName; }
    public String getStoredFileName()    { return storedFileName; }
    public String getContentType()       { return contentType; }
    public long getFileSize()            { return fileSize; }
    public Task getTask()                { return task; }
    public User getUploadedBy()          { return uploadedBy; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
}
