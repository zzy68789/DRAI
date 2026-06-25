package com.zzy.drai.agent.node;

import com.zzy.drai.agent.state.ResearchState;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

public class RouterNode {
    private static final List<String> REFINE_TRIGGERS = List.of(
            "修改", "改写", "扩写", "润色", "补充", "重写", "调整", "改成", "更通俗", "更详细", "删", "加"
    );

    private final BiFunction<Long, String, Optional<String>> latestReportResolver;

    public RouterNode(BiFunction<Long, String, Optional<String>> latestReportResolver) {
        this.latestReportResolver = latestReportResolver;
    }

    public String route(ResearchState state) {
        Optional<String> latestReport = latestReportResolver.apply(state.ownerId(), state.threadId());
        if (latestReport.isEmpty() || latestReport.get().isBlank()) {
            return "planner";
        }
        return looksLikeRefine(state.query()) ? "refiner" : "planner";
    }

    boolean looksLikeRefine(String query) {
        if (query == null || query.isBlank()) {
            return false;
        }
        return REFINE_TRIGGERS.stream().anyMatch(query::contains);
    }
}
