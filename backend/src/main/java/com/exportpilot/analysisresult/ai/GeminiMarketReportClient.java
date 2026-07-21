package com.exportpilot.analysisresult.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

@Component
public class GeminiMarketReportClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String apiKey;
    private final String model;
    private final int maxOutputTokens;

    public GeminiMarketReportClient(
            @Value("${gemini.api.base-url}") String baseUrl,
            @Value("${gemini.api.key:}") String apiKey,
            @Value("${gemini.api.model:gemini-3.1-flash-lite}") String model,
            @Value("${gemini.api.max-output-tokens:1200}") int maxOutputTokens
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();

        this.apiKey = apiKey;
        this.model = model;
        this.maxOutputTokens = maxOutputTokens;
    }

    public String generateReport(String prompt) {
        return generateText(
                prompt,
                0.3,
                maxOutputTokens
        );
    }

    public String generateChatAnswer(String prompt) {
        return generateText(
                prompt,
                0.2,
                500
        );
    }

    private String generateText(
            String prompt,
            double temperature,
            int outputTokenLimit
    ) {
        validateApiKey();

        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException(
                    "Gemini prompt bos olamaz."
            );
        }

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of(
                                "role", "user",
                                "parts", List.of(
                                        Map.of("text", prompt)
                                )
                        )
                ),
                "generationConfig", Map.of(
                        "temperature", temperature,
                        "maxOutputTokens", outputTokenLimit
                )
        );

        try {
            String responseBody = restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/models/{model}:generateContent")
                            .queryParam("key", apiKey)
                            .build(model))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            return extractText(responseBody);

        } catch (RestClientResponseException exception) {
            throw new IllegalStateException(
                    "Gemini API istegi basarisiz oldu. HTTP "
                            + exception.getStatusCode()
                            + ": "
                            + exception.getResponseBodyAsString(),
                    exception
            );
        }
    }

    private String extractText(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            throw new IllegalStateException(
                    "Gemini API bos bir cevap dondurdu."
            );
        }

        try {
            JsonNode root = objectMapper.readTree(responseBody);

            JsonNode textNode = root.path("candidates")
                    .path(0)
                    .path("content")
                    .path("parts")
                    .path(0)
                    .path("text");

            if (!textNode.isTextual() || textNode.asText().isBlank()) {
                throw new IllegalStateException(
                        "Gemini API cevabinda metin bulunamadi."
                );
            }

            return textNode.asText();

        } catch (IllegalStateException exception) {
            throw exception;

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Gemini API cevabi okunamadi.",
                    exception
            );
        }
    }

    private void validateApiKey() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "GEMINI_API_KEY ortam degiskeni tanimli degil."
            );
        }
    }

    public String getModel() {
        return model;
    }
}