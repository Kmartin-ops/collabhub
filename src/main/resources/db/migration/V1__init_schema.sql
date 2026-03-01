-- Users table
CREATE TABLE users (
                       id         UUID        NOT NULL DEFAULT gen_random_uuid(),
                       name       VARCHAR(100) NOT NULL,
                       email      VARCHAR(255) NOT NULL,
                       role       VARCHAR(50)  NOT NULL,
                       created_at TIMESTAMP    NOT NULL DEFAULT now(),

                       CONSTRAINT pk_users PRIMARY KEY (id),
                       CONSTRAINT uq_users_email UNIQUE (email)
);

-- Projects table
CREATE TABLE projects (
                          id          UUID         NOT NULL DEFAULT gen_random_uuid(),
                          name        VARCHAR(100) NOT NULL,
                          description VARCHAR(500) NOT NULL,
                          status      VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE',
                          created_at  TIMESTAMP    NOT NULL DEFAULT now(),

                          CONSTRAINT pk_projects PRIMARY KEY (id)
);

-- Project members join table (many-to-many)
CREATE TABLE project_members (
                                 project_id UUID NOT NULL,
                                 user_id    UUID NOT NULL,

                                 CONSTRAINT pk_project_members PRIMARY KEY (project_id, user_id),
                                 CONSTRAINT fk_pm_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
                                 CONSTRAINT fk_pm_user    FOREIGN KEY (user_id)    REFERENCES users(id)    ON DELETE CASCADE
);

-- Tasks table
CREATE TABLE tasks (
                       id          UUID         NOT NULL DEFAULT gen_random_uuid(),
                       title       VARCHAR(255) NOT NULL,
                       status      VARCHAR(50)  NOT NULL DEFAULT 'BACKLOG',
                       priority    VARCHAR(50)  NOT NULL,
                       due_date    DATE,
                       assignee_id UUID,
                       project_id  UUID         NOT NULL,
                       created_at  TIMESTAMP    NOT NULL DEFAULT now(),

                       CONSTRAINT pk_tasks      PRIMARY KEY (id),
                       CONSTRAINT fk_task_assignee FOREIGN KEY (assignee_id) REFERENCES users(id)    ON DELETE SET NULL,
                       CONSTRAINT fk_task_project  FOREIGN KEY (project_id)  REFERENCES projects(id) ON DELETE CASCADE
);

-- Comments table
CREATE TABLE comments (
                          id         UUID          NOT NULL DEFAULT gen_random_uuid(),
                          content    VARCHAR(1000) NOT NULL,
                          author_id  UUID          NOT NULL,
                          task_id    UUID          NOT NULL,
                          created_at TIMESTAMP     NOT NULL DEFAULT now(),

                          CONSTRAINT pk_comments     PRIMARY KEY (id),
                          CONSTRAINT fk_cm_author    FOREIGN KEY (author_id) REFERENCES users(id)  ON DELETE CASCADE,
                          CONSTRAINT fk_cm_task      FOREIGN KEY (task_id)   REFERENCES tasks(id)  ON DELETE CASCADE
);

-- Indexes for common query patterns
CREATE INDEX idx_tasks_project_id  ON tasks(project_id);
CREATE INDEX idx_tasks_assignee_id ON tasks(assignee_id);
CREATE INDEX idx_tasks_status      ON tasks(status);
CREATE INDEX idx_comments_task_id  ON comments(task_id);