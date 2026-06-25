package com.zzy.drai.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PlanParser {
    private final ObjectMapper objectMapper;

    public PlanParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<String> parse(String raw, String fallbackQuery) {
        List<String> parsed = parseJson(raw);
        if (parsed.isEmpty()) {
            parsed = parseDelimited(raw);
        }
        if (parsed.isEmpty()) {
            parsed = List.of(fallbackQuery, fallbackQuery + " 最新进展", fallbackQuery + " 技术挑战");
        }
        return parsed.stream()
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .distinct()
                .limit(5)
                .toList();
    }

    private List<String> parseJson(String raw) {
        try {
            String json = extractJson(raw);
            JsonNode root = objectMapper.readTree(json);
            JsonNode items = root.isArray() ? root : root.path("plan");
            if (!items.isArray()) {
                return List.of();
            }
            List<String> result = new ArrayList<>();
            items.forEach(item -> result.add(item.asText("")));
            return result;
        } catch (Exception e) {
            return List.of();
        }
    }

    private String extractJson(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("raw output is null");
        }
        int arrayStart = raw.indexOf('[');
        int arrayEnd = raw.lastIndexOf(']');
        int objectStart = raw.indexOf('{');
        int objectEnd = raw.lastIndexOf('}');
        if (arrayStart >= 0 && arrayEnd > arrayStart && (objectStart < 0 || arrayStart < objectStart)) {
            return raw.substring(arrayStart, arrayEnd + 1);
        }
        if (objectStart >= 0 && objectEnd > objectStart) {
            return raw.substring(objectStart, objectEnd + 1);
        }
        throw new IllegalArgumentException("json plan not found");
    }

    private List<String> parseDelimited(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        String[] pieces = raw.split("[,，\\n;；]+");
        List<String> result = new ArrayList<>();
        for (String piece : pieces) {
            String trimmed = piece.trim();
            if (!trimmed.isBlank()) {
                result.add(trimmed);
            }
        }
        return result;
    }
}
