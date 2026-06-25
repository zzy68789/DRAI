package com.zzy.drai.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PlanParserTest {

    @Test
    void parsesJsonArrayPlan() {
        PlanParser parser = new PlanParser(new ObjectMapper());

        List<String> plan = parser.parse("[\"背景\", \"技术路线\", \"风险\"]", "fallback");

        assertThat(plan).containsExactly("背景", "技术路线", "风险");
    }

    @Test
    void fallsBackToDelimitedTextWhenJsonIsInvalid() {
        PlanParser parser = new PlanParser(new ObjectMapper());

        List<String> plan = parser.parse("背景, 技术路线\n风险", "fallback");

        assertThat(plan).containsExactly("背景", "技术路线", "风险");
    }
}
