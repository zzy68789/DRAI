package com.zzy.drai.agent.node;

import com.zzy.drai.agent.state.AgentSubTaskResult;
import com.zzy.drai.agent.state.ResearchState;
import com.zzy.drai.rag.RagDocument;
import com.zzy.drai.rag.RagService;
import com.zzy.drai.search.SearchResult;
import com.zzy.drai.search.SearchService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class ResearcherNode {
    private static final double LOCAL_RELEVANCE_THRESHOLD = 0.2d;

    private final RagService ragService;
    private final SearchService searchService;

    public ResearcherNode(RagService ragService, SearchService searchService) {
        this.ragService = ragService;
        this.searchService = searchService;
    }

    public Map<String, Object> apply(ResearchState state) {
        String searchMode = state.searchMode();
        Set<String> deduplicated = new LinkedHashSet<>();
        List<AgentSubTaskResult> subTaskResults = new ArrayList<>();
        for (String query : researchQueries(state)) {
            long startedAt = System.currentTimeMillis();
            List<String> evidence = collectEvidence(query, searchMode);
            long durationMs = Math.max(0, System.currentTimeMillis() - startedAt);
            deduplicated.addAll(evidence);
            subTaskResults.add(evidence.isEmpty()
                    ? AgentSubTaskResult.noEvidence(query, durationMs)
                    : AgentSubTaskResult.completed(query, evidence, durationMs));
        }

        List<String> results = new ArrayList<>(deduplicated);
        if ("document".equalsIgnoreCase(searchMode) && results.isEmpty()) {
            return Map.of(
                    ResearchState.SEARCH_RESULTS, List.of("[WARNING] Document mode was selected, but no relevant local evidence was found."),
                    ResearchState.SUB_TASK_RESULTS, subTaskResults,
                    ResearchState.SHOULD_STOP, true
            );
        }

        return Map.of(
                ResearchState.SEARCH_RESULTS, results,
                ResearchState.SUB_TASK_RESULTS, subTaskResults,
                ResearchState.SHOULD_STOP, false
        );
    }

    private List<String> researchQueries(ResearchState state) {
        return state.plan().isEmpty() ? List.of(state.query()) : state.plan();
    }

    private List<String> collectEvidence(String query, String searchMode) {
        List<String> results = new ArrayList<>();
        List<RagDocument> ragDocs = "web".equalsIgnoreCase(searchMode)
                ? List.of()
                : ragService.retrieve(query, 5);
        boolean hasRelevantDocs = ragDocs.stream().anyMatch(doc -> doc.score() >= LOCAL_RELEVANCE_THRESHOLD);
        for (RagDocument doc : ragDocs) {
            results.add(localEvidence(doc));
        }

        if (shouldSearchWeb(searchMode, hasRelevantDocs)) {
            Set<String> deduplicated = new LinkedHashSet<>(results);
            List<SearchResult> webResults = searchService.search(query, 3);
            webResults.forEach(result -> deduplicated.add(webEvidence(query, result)));
            results = new ArrayList<>(deduplicated);
        }

        return results;
    }

    private boolean shouldSearchWeb(String searchMode, boolean hasRelevantDocs) {
        if ("web".equalsIgnoreCase(searchMode)) {
            return true;
        }
        return "hybrid".equalsIgnoreCase(searchMode) && !hasRelevantDocs;
    }

    private String localEvidence(RagDocument doc) {
        return "### LOCAL evidence (" + doc.source() + ", score=" + doc.score() + ")\n" + doc.content();
    }

    private String webEvidence(String query, SearchResult result) {
        return "### WEB evidence (" + query + ")\n"
                + "- source: " + result.source() + "\n"
                + "- title: " + result.title() + "\n"
                + "- url: " + result.url() + "\n"
                + result.content();
    }
}
