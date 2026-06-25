package com.zzy.drai.llm;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RetryingLlmClientTest {

    @Test
    void retriesFailedModelCallAndReturnsSuccessfulResponse() {
        FailingOnceLlmClient delegate = new FailingOnceLlmClient();
        RetryingLlmClient client = new RetryingLlmClient(delegate, 2);

        String response = client.generate("prompt", LlmClient.ModelType.FAST);

        assertThat(response).isEqualTo("ok");
        assertThat(delegate.attempts).isEqualTo(2);
    }

    @Test
    void returnsStructuredReviewFallbackAfterAllAttemptsFail() {
        RetryingLlmClient client = new RetryingLlmClient((prompt, modelType) -> {
            throw new IllegalStateException("downstream unavailable");
        }, 2);

        String response = client.generate("review", LlmClient.ModelType.SMART);

        assertThat(response).isEqualTo("{\"status\":\"FAIL\",\"feedback\":\"LLM 调用失败，请稍后重试或检查模型配置。\"}");
    }

    private static class FailingOnceLlmClient implements LlmClient {
        private int attempts;

        @Override
        public String generate(String prompt, ModelType modelType) {
            attempts++;
            if (attempts == 1) {
                throw new IllegalStateException("temporary");
            }
            return "ok";
        }
    }
}
