package com.matchguard.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.matchguard.backend.dto.AiScamDetectionResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class AiScamDetectionService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Value("${spring.ai.openai.base-url}")
    private String baseUrl;

    @Value("${spring.ai.openai.chat.options.model}")
    private String model;

    public AiScamDetectionService(
            RestTemplate restTemplate,
            ObjectMapper objectMapper
    ) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public AiScamDetectionResult analyzeProduct(
            String title,
            String description,
            Double price,
            String socialPostUrl
    ) {

        String prompt = buildPrompt(
                title,
                description,
                price,
                socialPostUrl
        );

        try {
            Map<String, Object> requestBody = new HashMap<>();

            requestBody.put("model", model);

            requestBody.put(
                    "messages",
                    new Object[]{
                            Map.of(
                                    "role", "user",
                                    "content", prompt
                            )
                    }
            );

            HttpHeaders headers = new HttpHeaders();

            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> request =
                    new HttpEntity<>(requestBody, headers);

            String response = restTemplate.postForObject(
                    baseUrl + "/chat/completions",
                    request,
                    String.class
            );

            return parseOpenRouterResponse(response);

        } catch (Exception e) {

            return new AiScamDetectionResult(
                    0,
                    "AI scam analysis failed: " + e.getMessage(),
                    false
            );
        }
    }

    private String buildPrompt(
            String title,
            String description,
            Double price,
            String socialPostUrl
    ) {

        return """
                You are an AI fraud and scam detection system
                for an online marketplace.

                Analyze the following product listing.

                PRODUCT TITLE:
                %s

                DESCRIPTION:
                %s

                PRICE:
                %s

                SOCIAL POST URL:
                %s

                Evaluate these risk areas:

                1. Scam indicators
                2. Suspicious or unrealistic pricing
                3. Urgency or pressure tactics
                4. Suspicious payment instructions
                5. High-risk wording
                6. Unrealistic product claims
                7. Potential counterfeit or impersonation indicators
                8. Other marketplace scam behavior

                Return ONLY valid JSON.

                The JSON MUST have exactly these fields:

                {
                  "trustScore": number,
                  "scamAnalysisSummary": "string",
                  "isVerifiedSafe": boolean
                }

                trustScore must be an integer between 0 and 100.

                0 means extremely high risk.
                100 means very trustworthy.

                isVerifiedSafe should only be true when
                there are no significant scam indicators.

                Do not include markdown.
                Do not include ```json.
                Do not include any text outside the JSON.
                """.formatted(
                title,
                description,
                price,
                socialPostUrl
        );
    }

    private AiScamDetectionResult parseOpenRouterResponse(
            String response
    ) throws Exception {

        JsonNode root = objectMapper.readTree(response);

        JsonNode contentNode = root
                .path("choices")
                .path(0)
                .path("message")
                .path("content");

        if (contentNode.isMissingNode()) {
            throw new RuntimeException(
                    "Invalid OpenRouter response: AI content not found"
            );
        }

        String jsonText = contentNode.asText()
                .replace("```json", "")
                .replace("```", "")
                .trim();

        JsonNode result = objectMapper.readTree(jsonText);

        Integer trustScore = result
                .path("trustScore")
                .asInt();

        String summary = result
                .path("scamAnalysisSummary")
                .asText();

        Boolean verifiedSafe = result
                .path("isVerifiedSafe")
                .asBoolean();

        trustScore = Math.max(
                0,
                Math.min(100, trustScore)
        );

        return new AiScamDetectionResult(
                trustScore,
                summary,
                verifiedSafe
        );
    }
}