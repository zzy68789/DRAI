package com.zzy.drai.controller;

import com.zzy.drai.auth.UserContext;
import com.zzy.drai.dto.AgentStepLogResponse;
import com.zzy.drai.dto.ApiResponse;
import com.zzy.drai.dto.PageResponse;
import com.zzy.drai.dto.ReportIndexResponse;
import com.zzy.drai.dto.ReportResponse;
import com.zzy.drai.dto.TaskDetailResponse;
import com.zzy.drai.dto.TaskSummaryResponse;
import com.zzy.drai.service.ExportedReport;
import com.zzy.drai.service.ReportExportService;
import com.zzy.drai.service.TaskQueryService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TaskQueryController {
    private final TaskQueryService taskQueryService;
    private final ReportExportService reportExportService;
    private final UserContext userContext;

    public TaskQueryController(TaskQueryService taskQueryService, ReportExportService reportExportService, UserContext userContext) {
        this.taskQueryService = taskQueryService;
        this.reportExportService = reportExportService;
        this.userContext = userContext;
    }

    @GetMapping("/tasks")
    public ApiResponse<PageResponse<TaskSummaryResponse>> listTasks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.success(taskQueryService.listTasks(userContext.currentUserId(), page, size, status, keyword));
    }

    @GetMapping("/tasks/{taskId}")
    public ApiResponse<TaskDetailResponse> getTask(@PathVariable long taskId) {
        return ApiResponse.success(taskQueryService.getTask(userContext.currentUserId(), taskId));
    }

    @GetMapping("/tasks/{taskId}/logs")
    public ApiResponse<List<AgentStepLogResponse>> getTaskLogs(@PathVariable long taskId) {
        return ApiResponse.success(taskQueryService.getTaskLogs(userContext.currentUserId(), taskId));
    }

    @GetMapping("/threads/{threadId}/reports")
    public ApiResponse<List<ReportResponse>> getThreadReports(@PathVariable String threadId) {
        return ApiResponse.success(taskQueryService.getThreadReports(userContext.currentUserId(), threadId));
    }

    @GetMapping("/reports")
    public ApiResponse<List<ReportResponse>> listReports(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "false") boolean favoriteOnly
    ) {
        return ApiResponse.success(taskQueryService.listReports(userContext.currentUserId(), keyword, favoriteOnly));
    }

    @GetMapping("/reports/{reportId}")
    public ApiResponse<ReportResponse> getReport(@PathVariable long reportId) {
        return ApiResponse.success(taskQueryService.getReport(userContext.currentUserId(), reportId));
    }

    @GetMapping("/reports/{reportId}/export")
    public ResponseEntity<byte[]> exportReport(
            @PathVariable long reportId,
            @RequestParam(defaultValue = "pdf") String format
    ) {
        ReportResponse report = taskQueryService.getReport(userContext.currentUserId(), reportId);
        ExportedReport exported = reportExportService.export(report, format);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(exported.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(exported.filename())
                        .build()
                        .toString())
                .body(exported.bytes());
    }

    @PostMapping("/reports/{reportId}/favorite")
    public ApiResponse<ReportResponse> updateFavorite(
            @PathVariable long reportId,
            @RequestParam(defaultValue = "true") boolean favorite
    ) {
        return ApiResponse.success(taskQueryService.updateFavorite(userContext.currentUserId(), reportId, favorite));
    }

    @DeleteMapping("/reports/{reportId}")
    public ApiResponse<Void> deleteReport(@PathVariable long reportId) {
        taskQueryService.deleteReport(userContext.currentUserId(), reportId);
        return ApiResponse.success(null);
    }

    @PostMapping("/reports/{reportId}/knowledge-base")
    public ApiResponse<ReportIndexResponse> indexReportToKnowledgeBase(@PathVariable long reportId) {
        return ApiResponse.success(taskQueryService.indexReportToKnowledgeBase(userContext.currentUserId(), reportId));
    }
}
