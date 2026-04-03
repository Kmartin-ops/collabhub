package com.collabhub.repository;

import com.collabhub.dto.SearchResult;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class SearchRepository {

    private final JdbcTemplate jdbc;

    public SearchRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<SearchResult> search(String query, int limit) {
        List<SearchResult> results = new ArrayList<>();

        // Tasks — full-text + trigram fallback
        String taskSql = """
            SELECT t.id, t.title, t.status, t.priority, t.project_id
            FROM tasks t
            WHERE
                to_tsvector('english', coalesce(t.title,'') || ' ' || coalesce(t.status,'') || ' ' || coalesce(t.priority,''))
                @@ plainto_tsquery('english', ?)
                OR t.title ILIKE ?
            ORDER BY ts_rank(
                to_tsvector('english', coalesce(t.title,'')),
                plainto_tsquery('english', ?)
            ) DESC
            LIMIT ?
            """;

        results.addAll(jdbc.query(taskSql,
                (rs, i) -> new SearchResult(
                        UUID.fromString(rs.getString("id")),
                        "TASK",
                        rs.getString("title"),
                        rs.getString("priority") + " · " + rs.getString("status"),
                        "/projects/" + rs.getString("project_id") + "/tasks"
                ),
                query, "%" + query + "%", query, limit));

        // Projects — full-text + trigram fallback
        String projectSql = """
            SELECT p.id, p.name, p.status, p.description
            FROM projects p
            WHERE
                to_tsvector('english', coalesce(p.name,'') || ' ' || coalesce(p.description,'') || ' ' || coalesce(p.status,''))
                @@ plainto_tsquery('english', ?)
                OR p.name ILIKE ?
            ORDER BY ts_rank(
                to_tsvector('english', coalesce(p.name,'')),
                plainto_tsquery('english', ?)
            ) DESC
            LIMIT ?
            """;

        results.addAll(jdbc.query(projectSql,
                (rs, i) -> new SearchResult(
                        UUID.fromString(rs.getString("id")),
                        "PROJECT",
                        rs.getString("name"),
                        rs.getString("status"),
                        "/projects/" + rs.getString("id")
                ),
                query, "%" + query + "%", query, limit));

        return results;
    }
}
