-- V6__attachments.sql
CREATE TABLE attachments (
                             id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                             original_file_name VARCHAR(255) NOT NULL,
                             stored_file_name   VARCHAR(255) NOT NULL,
                             content_type       VARCHAR(100) NOT NULL,
                             file_size          BIGINT       NOT NULL DEFAULT 0,
                             task_id            UUID         NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
                             uploaded_by        UUID         REFERENCES users(id) ON DELETE SET NULL,
                             uploaded_at        TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_attachments_task_id ON attachments(task_id);
