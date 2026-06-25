package com.zzy.drai.agent.node;

import com.zzy.drai.agent.state.ResearchState;
import com.zzy.drai.rag.RagDocument;
import com.zzy.drai.rag.RagService;
import com.zzy.drai.search.SearchResult;
import com.zzy.drai.search.SearchService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ResearcherNodeTest {

    @Test
    void hybridModeUsesLocalEvidenceOnlyWhenItIsRelevantEnough() {
        RagService ragService = mock(RagService.class);
        SearchService searchService = mock(SearchService.class);
        ResearcherNode node = new ResearcherNode(ragService, searchService);
        when(ragService.retrieve("agent workflow", 5))
                .thenReturn(List.of(new RagDocument("agent.pdf", "local agent evidence", 0.72)));

        Map<String, Object> result = node.apply(new ResearchState(Map.of(
                ResearchState.QUERY, "agent workflow",
                ResearchState.SEARCH_MODE, "hybrid"
        )));

        @SuppressWarnings("unchecked")
        List<String> searchResults = (List<String>) result.get(ResearchState.SEARCH_RESULTS);
        assertThat(searchResults).hasSize(1);
        assertThat(searchResults.get(0)).contains("LOCAL", "agent.pdf", "local agent evidence");
        verify(searchService, never()).search("agent workflow", 3);
    }

    @Test
    void hybridModeAddsWebResultsWhenLocalEvidenceIsWeak() {
        RagService ragService = mock(RagService.class);
        SearchService searchService = mock(SearchService.class);
        ResearcherNode node = new ResearcherNode(ragService, searchService);
        when(ragService.retrieve("agent workflow", 5))
                .thenReturn(List.of(new RagDocument("agent.pdf", "weak local evidence", 0.10)));
        when(searchService.search("agent workflow", 3))
                .thenReturn(List.of(new SearchResult("web title", "https://example.com", "web evidence")));

        Map<String, Object> result = node.apply(new ResearchState(Map.of(
                ResearchState.QUERY, "agent workflow",
                ResearchState.SEARCH_MODE, "hybrid"
        )));

        @SuppressWarnings("unchecked")
        List<String> searchResults = (List<String>) result.get(ResearchState.SEARCH_RESULTS);
        assertThat(searchResults).hasSize(2);
        assertThat(searchResults.get(0)).contains("LOCAL", "agent.pdf");
        assertThat(searchResults.get(1)).contains("WEB", "web title", "https://example.com", "web evidence");
    }

    @Test
    void webModeSkipsLocalRagAndOnlyUsesSearchResults() {
        RagService ragService = mock(RagService.class);
        SearchService searchService = mock(SearchService.class);
        ResearcherNode node = new ResearcherNode(ragService, searchService);
        when(searchService.search("agent workflow", 3))
                .thenReturn(List.of(new SearchResult("web title", "https://example.com", "web evidence")));

        Map<String, Object> result = node.apply(new ResearchState(Map.of(
                ResearchState.QUERY, "agent workflow",
                ResearchState.SEARCH_MODE, "web"
        )));

        @SuppressWarnings("unchecked")
        List<String> searchResults = (List<String>) result.get(ResearchState.SEARCH_RESULTS);
        assertThat(searchResults).hasSize(1);
        assertThat(searchResults.get(0)).contains("WEB", "web title");
        verify(ragService, never()).retrieve("agent workflow", 5);
    }
}
