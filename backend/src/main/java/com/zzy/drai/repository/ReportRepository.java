package com.zzy.drai.repository;

import com.zzy.drai.domain.ReportRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class ReportRepository {
    private static final RowMapper<ReportRecord> REPORT_MAPPER = (rs, rowNum) -> new ReportRecord(
            rs.getLong("id"),
            rs.getObject("task_id", Long.class),
            rs.getString("thread_id"),
            rs.getString("content"),
            rs.getInt("version"),
            rs.getString("review_status"),
            rs.getString("critique"),
            rs.getObject("created_at", LocalDateTime.class),
            rs.getBoolean("favorite"),
            rs.getObject("indexed_at", LocalDateTime.class)
    );

    private final JdbcClient jdbcClient;
    private final JdbcTemplate jdbcTemplate;

    public ReportRepository(JdbcClient jdbcClient, JdbcTemplate jdbcTemplate) {
        this.jdbcClient = jdbcClient;
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(long ownerId, long taskId, String threadId, String content, String reviewStatus, String critique) {
        Integer latestVersion = jdbcClient.sql("SELECT COALESCE(MAX(version), 0) FROM report WHERE owner_id = :ownerId AND thread_id = :threadId")
                .param("ownerId", ownerId)
                .param("threadId", threadId)
                .query(Integer.class)
                .single();
        jdbcClient.sql("""
                        INSERT INTO report(owner_id, task_id, thread_id, content, version, review_status, critique, favorite, created_at)
                        VALUES (:ownerId, :taskId, :threadId, :content, :version, :reviewStatus, :critique, false, :createdAt)
                        """)
                .param("ownerId", ownerId)
                .param("taskId", taskId)
                .param("threadId", threadId)
                .param("content", content)
                .param("version", latestVersion + 1)
                .param("reviewStatus", reviewStatus)
                .param("critique", critique)
                .param("createdAt", LocalDateTime.now())
                .update();
    }

    public Optional<String> findLatestByThread(long ownerId, String threadId) {
        return jdbcClient.sql("""
                        SELECT content FROM report
                        WHERE owner_id = :ownerId AND thread_id = :threadId
                          AND deleted_at IS NULL
                        ORDER BY version DESC, id DESC
                        LIMIT 1
                        """)
                .param("ownerId", ownerId)
                .param("threadId", threadId)
                .query(String.class)
                .optional();
    }

    public List<ReportRecord> findReportsByThread(long ownerId, String threadId) {
        return jdbcTemplate.query("""
                        SELECT id, task_id, thread_id, content, version, review_status, critique, created_at, favorite, indexed_at
                        FROM report
                        WHERE owner_id = ? AND thread_id = ?
                          AND deleted_at IS NULL
                        ORDER BY version DESC, id DESC
                        """,
                REPORT_MAPPER,
                ownerId,
                threadId
        );
    }

    public Optional<ReportRecord> findReportById(long ownerId, long reportId) {
        return jdbcTemplate.query("""
                        SELECT id, task_id, thread_id, content, version, review_status, critique, created_at, favorite, indexed_at
                        FROM report
                        WHERE owner_id = ? AND id = ? AND deleted_at IS NULL
                        """,
                REPORT_MAPPER,
                ownerId,
                reportId
        ).stream().findFirst();
    }

    public List<ReportRecord> findReports(long ownerId, String keyword, boolean favoriteOnly) {
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : "%" + keyword.trim() + "%";
        return jdbcClient.sql("""
                        SELECT id, task_id, thread_id, content, version, review_status, critique, created_at, favorite, indexed_at
                        FROM report
                        WHERE owner_id = :ownerId
                          AND deleted_at IS NULL
                          AND (:favoriteOnly = false OR favorite = true)
                          AND (:keyword IS NULL OR thread_id LIKE :keyword OR content LIKE :keyword)
                        ORDER BY created_at DESC, id DESC
                        LIMIT 100
                        """)
                .param("ownerId", ownerId)
                .param("favoriteOnly", favoriteOnly)
                .param("keyword", normalizedKeyword)
                .query(REPORT_MAPPER)
                .list();
    }

    public void updateFavorite(long ownerId, long reportId, boolean favorite) {
        jdbcClient.sql("""
                        UPDATE report
                        SET favorite = :favorite
                        WHERE owner_id = :ownerId AND id = :reportId AND deleted_at IS NULL
                        """)
                .param("ownerId", ownerId)
                .param("reportId", reportId)
                .param("favorite", favorite)
                .update();
    }

    public void softDelete(long ownerId, long reportId) {
        jdbcClient.sql("""
                        UPDATE report
                        SET deleted_at = :deletedAt
                        WHERE owner_id = :ownerId AND id = :reportId AND deleted_at IS NULL
                        """)
                .param("ownerId", ownerId)
                .param("reportId", reportId)
                .param("deletedAt", LocalDateTime.now())
                .update();
    }

    public void markIndexed(long ownerId, long reportId) {
        jdbcClient.sql("""
                        UPDATE report
                        SET indexed_at = :indexedAt
                        WHERE owner_id = :ownerId AND id = :reportId AND deleted_at IS NULL
                        """)
                .param("ownerId", ownerId)
                .param("reportId", reportId)
                .param("indexedAt", LocalDateTime.now())
                .update();
    }
}
