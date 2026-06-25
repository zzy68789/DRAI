package com.zzy.drai.agent.node;

import com.zzy.drai.agent.state.AgentSubTaskResult;
import com.zzy.drai.agent.state.ResearchState;
import com.zzy.drai.llm.LlmClient;
import com.zzy.drai.llm.PlanParser;
import com.zzy.drai.llm.PromptTemplates;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class PlannerNode {
    private final LlmClient llmClient;
    private final PlanParser planParser;

    public PlannerNode(LlmClient llmClient, PlanParser planParser) {
        this.llmClient = llmClient;
        this.planParser = planParser;
    }

    public Map<String, Object> apply(ResearchState state) {
        String raw = llmClient.generate(PromptTemplates.planner(state.query(), state.critique()), LlmClient.ModelType.FAST);
        List<String> plan = planParser.parse(raw, state.query());
        List<AgentSubTaskResult> subTasks = plan.stream()
                .map(query -> new AgentSubTaskResult(query, "PENDING", List.of(), 0, 0, ""))
                .toList();
        return Map.of(
                ResearchState.PLAN, plan,
                ResearchState.SUB_TASK_RESULTS, subTasks
        );
    }
}
