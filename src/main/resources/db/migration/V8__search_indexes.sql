-- V8__search_indexes.sql
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS unaccent;

-- Add tsvector columns for full-text search
ALTER TABLE tasks    ADD COLUMN IF NOT EXISTS search_vector tsvector;
ALTER TABLE projects ADD COLUMN IF NOT EXISTS search_vector tsvector;

-- Populate initial values
UPDATE tasks    SET search_vector = to_tsvector('english', coalesce(title, '') || ' ' || coalesce(status, '') || ' ' || coalesce(priority, ''));
UPDATE projects SET search_vector = to_tsvector('english', coalesce(name, '') || ' ' || coalesce(description, '') || ' ' || coalesce(status, ''));

-- GIN indexes for fast full-text search
CREATE INDEX IF NOT EXISTS idx_tasks_search    ON tasks    USING GIN(search_vector);
CREATE INDEX IF NOT EXISTS idx_projects_search ON projects USING GIN(search_vector);

-- Trigram indexes for partial/fuzzy matching
CREATE INDEX IF NOT EXISTS idx_tasks_title_trgm    ON tasks    USING GIN(title    gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_projects_name_trgm  ON projects USING GIN(name     gin_trgm_ops);
