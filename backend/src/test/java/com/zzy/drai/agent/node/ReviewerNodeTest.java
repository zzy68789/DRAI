package com.zzy.drai.agent.node;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzy.drai.agent.state.ResearchState;
import com.zzy.drai.llm.LlmClient;
import com.zzy.drai.llm.ReviewResultParser;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class ReviewerNodeTest {

    @Test
    void failsClosedWhenReportHasNoEvidence() {
        LlmClient llmClient = mock(LlmClient.class);
        ReviewerNode node = new ReviewerNode(llmClient, new ReviewResultParser(new ObjectMapper()));

        Map<String, Object> result = node.apply(new ResearchState(Map.of(
                ResearchState.QUERY, "agent workflow",
                ResearchState.FINAL_REPORT, "# Report\nNo cited evidence.",
                ResearchState.SEARCH_RESULTS, List.of(),
                ResearchState.REVISION_NUMBER, 0
        )));

        assertThat(result).containsEntry(ResearchState.REVIEW_STATUS, "FAIL");
        assertThat(result.get(ResearchState.CRITIQUE).toString()).contains("证据不足");
        assertThat(result).containsEntry(ResearchState.REVISION_NUMBER, 1);
        verify(llmClient, never()).generate(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any());
    }
}
