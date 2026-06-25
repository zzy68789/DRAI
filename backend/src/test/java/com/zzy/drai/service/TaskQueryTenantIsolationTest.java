package com.zzy.drai.service;

import com.zzy.drai.domain.ResearchTaskRecord;
import com.zzy.drai.dto.PageResponse;
import com.zzy.drai.dto.TaskSummaryResponse;
import com.zzy.drai.repository.AgentStepLogRepository;
import com.zzy.drai.repository.ReportRepository;
import com.zzy.drai.repository.ResearchTaskRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TaskQueryTenantIsolationTest {

    @Test
    void listTasksUsesOwnerScopedRepositoryMethods() {
        ResearchTaskRepository taskRepository = mock(ResearchTaskRepository.class);
        TaskQueryService service = new TaskQueryService(
                taskRepository,
                mock(AgentStepLogRepository.class),
                mock(ReportRepository.class)
        );
        LocalDateTime now = LocalDateTime.now();
        when(taskRepository.findPage(7L, 1, 10, "COMPLETED", "agent"))
                .thenReturn(List.of(new ResearchTaskRecord(1L, "thread-1", "agent", "hybrid", "COMPLETED", 0, now, now)));
        when(taskRepository.count(7L, "COMPLETED", "agent")).thenReturn(1L);

        PageResponse<TaskSummaryResponse> page = service.listTasks(7L, 1, 10, "COMPLETED", "agent");

        assertThat(page.total()).isEqualTo(1L);
        assertThat(page.items()).singleElement().extracting(TaskSummaryResponse::threadId).isEqualTo("thread-1");
        verify(taskRepository).findPage(7L, 1, 10, "COMPLETED", "agent");
        verify(taskRepository).count(7L, "COMPLETED", "agent");
    }
}
