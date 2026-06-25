package com.zzy.drai.agent.state;

import java.io.Serializable;
import java.util.List;

public record AgentSubTaskResult(
        String query,
        String status,
        List<String> evidence,
        int evidenceCount,
        long durationMs,
        String errorReason
) implements Serializable {
    private static final long serialVersionUID = 1L;

    public static AgentSubTaskResult completed(String query, List<String> evidence, long durationMs) {
        return new AgentSubTaskResult(query, "COMPLETED", evidence, evidence.size(), durationMs, "");
    }

    public static AgentSubTaskResult noEvidence(String query, long durationMs) {
        return new AgentSubTaskResult(query, "NO_EVIDENCE", List.of(), 0, durationMs, "未检索到可用证据");
    }
}
