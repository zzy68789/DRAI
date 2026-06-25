package com.zzy.drai.service;

import com.zzy.drai.repository.ReportRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class ReportService {
    private final ReportRepository reportRepository;
    private final ConcurrentMap<String, String> latestReportByThread = new ConcurrentHashMap<>();

    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public Optional<String> findLatestByThread(long ownerId, String threadId) {
        Optional<String> persisted = reportRepository.findLatestByThread(ownerId, threadId);
        return persisted.isPresent() ? persisted : Optional.ofNullable(latestReportByThread.get(cacheKey(ownerId, threadId)));
    }

    public void saveLatest(long ownerId, String threadId, String report) {
        saveLatest(ownerId, threadId, 0L, report, "PASS", "");
    }

    public void saveLatest(long ownerId, String threadId, long taskId, String report, String reviewStatus, String critique) {
        if (threadId != null && report != null && !report.isBlank()) {
            latestReportByThread.put(cacheKey(ownerId, threadId), report);
            reportRepository.save(ownerId, taskId, threadId, report, reviewStatus, critique);
        }
    }

    private String cacheKey(long ownerId, String threadId) {
        return ownerId + ":" + threadId;
    }
}
