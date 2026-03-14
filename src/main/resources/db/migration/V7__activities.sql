-- V7__activities.sql
CREATE TABLE activities (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    action      VARCHAR(50)  NOT NULL,
    actor_name  VARCHAR(255) NOT NULL,
    entity_type VARCHAR(50),
    entity_id   UUID,
    entity_name VARCHAR(255),
    detail      VARCHAR(500),
    project_id  UUID         NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_activities_project_id  ON activities(project_id);
CREATE INDEX idx_activities_created_at  ON activities(created_at DESC);
