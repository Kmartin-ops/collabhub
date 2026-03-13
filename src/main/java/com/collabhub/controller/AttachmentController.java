package com.collabhub.controller;

import com.collabhub.domain.Attachment;
import com.collabhub.dto.AttachmentResponse;
import com.collabhub.repository.AttachmentRepository;
import com.collabhub.service.StorageService;
import com.collabhub.service.TaskService;
import com.collabhub.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks/{taskId}/attachments")
@Tag(name = "Attachments", description = "File attachment endpoints")
public class AttachmentController {

    private final AttachmentRepository attachmentRepository;
    private final StorageService       storageService;
    private final TaskService          taskService;
    private final UserService          userService;

    public AttachmentController(AttachmentRepository attachmentRepository,
                                StorageService storageService,
                                TaskService taskService,
                                UserService userService) {
        this.attachmentRepository = attachmentRepository;
        this.storageService       = storageService;
        this.taskService          = taskService;
        this.userService          = userService;
    }

    @GetMapping
    @Operation(summary = "List attachments for a task")
    public List<AttachmentResponse> list(@PathVariable UUID taskId) {
        return attachmentRepository.findByTaskId(taskId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a file attachment to a task")
    public ResponseEntity<AttachmentResponse> upload(
            @PathVariable UUID taskId,
            @RequestPart("file") MultipartFile file,
            Authentication auth) throws IOException {

        var task      = taskService.getById(taskId);
        var uploader  = userService.getByEmail(auth.getName());
        var stored    = storageService.store(file);

        var attachment = new Attachment(
                file.getOriginalFilename(),
                stored,
                file.getContentType(),
                file.getSize(),
                task,
                uploader
        );
        attachmentRepository.save(attachment);

        return ResponseEntity
                .created(URI.create("/api/tasks/" + taskId + "/attachments/" + attachment.getId()))
                .body(toResponse(attachment));
    }

    @GetMapping("/{attachmentId}/download")
    @Operation(summary = "Download an attachment")
    public ResponseEntity<InputStreamResource> download(
            @PathVariable UUID taskId,
            @PathVariable UUID attachmentId) throws IOException {

        var attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new com.collabhub.exception.ResourceNotFoundException("Attachment", attachmentId));

        var stream = storageService.load(attachment.getStoredFileName());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(attachment.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + attachment.getOriginalFileName() + "\"")
                .body(new InputStreamResource(stream));
    }

    @DeleteMapping("/{attachmentId}")
    @Operation(summary = "Delete an attachment")
    public ResponseEntity<Void> delete(
            @PathVariable UUID taskId,
            @PathVariable UUID attachmentId) throws IOException {

        var attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new com.collabhub.exception.ResourceNotFoundException("Attachment", attachmentId));

        storageService.delete(attachment.getStoredFileName());
        attachmentRepository.delete(attachment);
        return ResponseEntity.noContent().build();
    }

    private AttachmentResponse toResponse(Attachment a) {
        return new AttachmentResponse(
                a.getId(),
                a.getOriginalFileName(),
                a.getContentType(),
                a.getFileSize(),
                a.getUploadedBy() != null ? a.getUploadedBy().getName() : null,
                a.getUploadedAt(),
                "/api/tasks/" + a.getTask().getId() + "/attachments/" + a.getId() + "/download"
        );
    }
}
