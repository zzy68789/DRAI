package com.zzy.drai.repository;

import com.zzy.drai.domain.ResearchTaskRecord;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ResearchTaskRepository {
    private static final RowMapper<ResearchTaskRecord> TASK_MAPPER = (rs, rowNum) -> new ResearchTaskRecord(
            rs.getLong("id"),
            rs.getString("thread_id"),
            rs.getString("query"),
            rs.getString("search_mode"),
            rs.getString("status"),
            rs.getInt("revision_number"),
            rs.getObject("created_at", LocalDateTime.class),
            rs.getObject("updated_at", LocalDateTime.class)
    );

    private final JdbcClient jdbcClient;
    private final JdbcTemplate jdbcTemplate;

    public ResearchTaskRepository(JdbcClient jdbcClient, JdbcTemplate jdbcTemplate) {
        this.jdbcClient = jdbcClient;
        this.jdbcTemplate = jdbcTemplate;
    }

    public long create(long ownerId, String threadId, String query, String searchMode) {
        LocalDateTime now = LocalDateTime.now();
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO research_task(owner_id, thread_id, query, search_mode, status, revision_number, created_at, updated_at)
                    VALUES (?, ?, ?, ?, ?, 0, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, ownerId);
            statement.setString(2, threadId);
            statement.setString(3, query);
            statement.setString(4, searchMode);
            statement.setString(5, "CREATED");
            statement.setObject(6, now);
            statement.setObject(7, now);
            return statement;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("创建调研任务后未返回生成主键");
        }
        return key.longValue();
    }

    public void markRunning(long taskId) {
        updateStatus(taskId, "RUNNING");
    }

    public void markCompleted(long taskId) {
        updateStatus(taskId, "COMPLETED");
    }

    public void markFailed(long taskId) {
        updateStatus(taskId, "FAILED");
    }

    private void updateStatus(long taskId, String status) {
        jdbcClient.sql("UPDATE research_task SET status = :status, updated_at = :now WHERE id = :id")
                .param("status", status)
                .param("now", LocalDateTime.now())
                .param("id", taskId)
                .update();
    }

    public List<ResearchTaskRecord> findPage(long ownerId, int page, int size, String status, String keyword) {
        QueryParts queryParts = buildFilter(ownerId, status, keyword);
        List<Object> params = new ArrayList<>(queryParts.params());
        params.add(size);
        params.add((page - 1) * size);
        return jdbcTemplate.query("""
                        SELECT id, thread_id, query, search_mode, status, revision_number, created_at, updated_at
                        FROM research_task
                        %s
                        ORDER BY updated_at DESC, id DESC
                        LIMIT ? OFFSET ?
                        """.formatted(queryParts.whereClause()),
                TASK_MAPPER,
                params.toArray()
        );
    }

    public long count(long ownerId, String status, String keyword) {
        QueryParts queryParts = buildFilter(ownerId, status, keyword);
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM research_task " + queryParts.whereClause(),
                Long.class,
                queryParts.params().toArray()
        );
        return count == null ? 0L : count;
    }

    public Optional<ResearchTaskRecord> findById(long ownerId, long taskId) {
        return jdbcTemplate.query("""
                        SELECT id, thread_id, query, search_mode, status, revision_number, created_at, updated_at
                        FROM research_task
                        WHERE owner_id = ? AND id = ?
                        """,
                TASK_MAPPER,
                ownerId,
                taskId
        ).stream().findFirst();
    }

    private QueryParts buildFilter(long ownerId, String status, String keyword) {
        List<String> conditions = new ArrayList<>(List.of("owner_id = ?"));
        List<Object> params = new ArrayList<>(List.of(ownerId));
        if (status != null && !status.isBlank()) {
            conditions.add("status = ?");
            params.add(status);
        }
        if (keyword != null && !keyword.isBlank()) {
            conditions.add("(query LIKE ? OR thread_id LIKE ?)");
            String pattern = "%" + keyword.trim() + "%";
            params.add(pattern);
            params.add(pattern);
        }
        String whereClause = conditions.isEmpty() ? "" : "WHERE " + String.join(" AND ", conditions);
        return new QueryParts(whereClause, params);
    }

    private record QueryParts(String whereClause, List<Object> params) {
    }
}
