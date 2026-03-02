-- Additional indexes for common query patterns identified in M3

-- Projects filtered by status (getAllProjects with status filter)
CREATE INDEX IF NOT EXISTS idx_projects_status
    ON projects(status);

-- Tasks filtered by status within a project
CREATE INDEX IF NOT EXISTS idx_tasks_project_status
    ON tasks(project_id, status);

-- Tasks filtered by priority within a project
CREATE INDEX IF NOT EXISTS idx_tasks_project_priority
    ON tasks(project_id, priority);

-- Tasks filtered by due date (overdue queries)
CREATE INDEX IF NOT EXISTS idx_tasks_due_date
    ON tasks(due_date);

-- Comments ordered by creation time per task
CREATE INDEX IF NOT EXISTS idx_comments_task_created
    ON comments(task_id, created_at);

-- Users looked up by role
CREATE INDEX IF NOT EXISTS idx_users_role
    ON users(role);