package com.zzy.drai.agent.node;

import com.zzy.drai.agent.state.ResearchState;
import com.zzy.drai.llm.LlmClient;
import com.zzy.drai.llm.PromptTemplates;
import com.zzy.drai.llm.ReviewResult;
import com.zzy.drai.llm.ReviewResultParser;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ReviewerNode {
    private final LlmClient llmClient;
    private final ReviewResultParser reviewResultParser;

    public ReviewerNode(LlmClient llmClient, ReviewResultParser reviewResultParser) {
        this.llmClient = llmClient;
        this.reviewResultParser = reviewResultParser;
    }

    public Map<String, Object> apply(ResearchState state) {
        String raw = llmClient.generate(
                PromptTemplates.reviewer(state.query(), state.finalReport().orElse("")),
                LlmClient.ModelType.SMART
        );
        ReviewResult review = reviewResultParser.parse(raw);
        return Map.of(
                ResearchState.REVIEW_STATUS, review.status(),
                ResearchState.CRITIQUE, review.feedback(),
                ResearchState.REVISION_NUMBER, state.revisionNumber() + 1
        );
    }
}
