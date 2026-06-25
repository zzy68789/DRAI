package com.zzy.drai.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class ReviewResultParser {
    private final ObjectMapper objectMapper;

    public ReviewResultParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ReviewResult parse(String raw) {
        try {
            String json = extractJsonObject(raw);
            JsonNode node = objectMapper.readTree(json);
            String status = node.path("status").asText("FAIL").trim().toUpperCase();
            String feedback = node.path("feedback").asText("");
            if (!"PASS".equals(status) && !"FAIL".equals(status)) {
                return failClosed("Reviewer 输出 JSON 中 status 只能是 PASS 或 FAIL。");
            }
            return new ReviewResult(status, feedback);
        } catch (Exception e) {
            return failClosed("Reviewer 输出不是合法 JSON，请重新生成结构更清晰的报告。");
        }
    }

    private ReviewResult failClosed(String feedback) {
        return new ReviewResult("FAIL", feedback);
    }

    private String extractJsonObject(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("raw output is null");
        }
        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');
        if (start < 0 || end < start) {
            throw new IllegalArgumentException("json object not found");
        }
        return raw.substring(start, end + 1);
    }
}
