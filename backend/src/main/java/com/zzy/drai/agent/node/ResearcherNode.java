package com.zzy.drai.agent.node;

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
        List<String> results = new ArrayList<>();
        List<RagDocument> ragDocs = "web".equalsIgnoreCase(searchMode)
                ? List.of()
                : ragService.retrieve(state.query(), 5);
        boolean hasRelevantDocs = ragDocs.stream().anyMatch(doc -> doc.score() >= LOCAL_RELEVANCE_THRESHOLD);
        for (RagDocument doc : ragDocs) {
            results.add(localEvidence(doc));
        }

        if ("document".equalsIgnoreCase(searchMode) && !hasRelevantDocs) {
            return Map.of(
                    ResearchState.SEARCH_RESULTS, List.of("[WARNING] Document mode was selected, but no relevant local evidence was found."),
                    ResearchState.SHOULD_STOP, true
            );
        }

        if (shouldSearchWeb(searchMode, hasRelevantDocs)) {
            Set<String> deduplicated = new LinkedHashSet<>(results);
            for (String query : state.plan().isEmpty() ? List.of(state.query()) : state.plan()) {
                List<SearchResult> webResults = searchService.search(query, 3);
                webResults.forEach(result -> deduplicated.add(webEvidence(query, result)));
            }
            results = new ArrayList<>(deduplicated);
        }

        return Map.of(
                ResearchState.SEARCH_RESULTS, results,
                ResearchState.SHOULD_STOP, false
        );
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
                + "- title: " + result.title() + "\n"
                + "- url: " + result.url() + "\n"
                + result.content();
    }
}
