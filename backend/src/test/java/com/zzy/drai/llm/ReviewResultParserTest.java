package com.zzy.drai.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewResultParserTest {

    @Test
    void parsesJsonObjectInsideMarkdownFence() {
        ReviewResultParser parser = new ReviewResultParser(new ObjectMapper());

        ReviewResult result = parser.parse("""
                ```json
                {"status":"PASS","feedback":""}
                ```
                """);

        assertThat(result.status()).isEqualTo("PASS");
        assertThat(result.feedback()).isBlank();
    }

    @Test
    void invalidReviewOutputFailsClosed() {
        ReviewResultParser parser = new ReviewResultParser(new ObjectMapper());

        ReviewResult result = parser.parse("not json");

        assertThat(result.status()).isEqualTo("FAIL");
        assertThat(result.feedback()).contains("Reviewer 输出不是合法 JSON");
    }
}
