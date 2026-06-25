package com.zzy.drai.config;

import com.zzy.drai.llm.LangChain4jLlmClient;
import com.zzy.drai.llm.LlmClient;
import com.zzy.drai.llm.RetryingLlmClient;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Duration;

@Configuration
public class LlmConfig {

    @Bean
    LangChain4jLlmClient langChain4jLlmClient(
            @Value("${drai.llm.base-url:https://api.openai.com/v1}") String baseUrl,
            @Value("${drai.llm.api-key:}") String apiKey,
            @Value("${drai.llm.fast-model:qwen3-max}") String fastModel,
            @Value("${drai.llm.smart-model:deepseek-r1}") String smartModel,
            @Value("${drai.llm.timeout:30s}") Duration timeout,
            @Value("${drai.llm.provider-max-retries:0}") int providerMaxRetries,
            @Value("${drai.llm.log-requests:false}") boolean logRequests,
            @Value("${drai.llm.log-responses:false}") boolean logResponses
    ) {
        return new LangChain4jLlmClient(
                buildModel(baseUrl, apiKey, fastModel, 0.7d, timeout, providerMaxRetries, logRequests, logResponses),
                buildModel(baseUrl, apiKey, smartModel, 0.0d, timeout, providerMaxRetries, logRequests, logResponses)
        );
    }

    @Bean
    @Primary
    LlmClient llmClient(
            LangChain4jLlmClient langChain4jLlmClient,
            @Value("${drai.llm.max-attempts:2}") int maxAttempts
    ) {
        return new RetryingLlmClient(langChain4jLlmClient, maxAttempts);
    }

    private ChatModel buildModel(
            String baseUrl,
            String apiKey,
            String modelName,
            double temperature,
            Duration timeout,
            int maxRetries,
            boolean logRequests,
            boolean logResponses
    ) {
        if (apiKey == null || apiKey.isBlank()) {
            return null;
        }
        return OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(temperature)
                .timeout(timeout)
                .maxRetries(maxRetries)
                .logRequests(logRequests)
                .logResponses(logResponses)
                .build();
    }
}
