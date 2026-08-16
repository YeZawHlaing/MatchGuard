package com.matchguard.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.matchguard.backend.dto.productDto.ProductRecommendationDto;
import com.matchguard.backend.entity.Product;
import com.matchguard.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiSearchService {

    private final ProductRepository productRepository;
    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;

    public List<ProductRecommendationDto> searchProducts(String userQuery) {
        // 1. Fetch active inventory from database
        List<Product> allProducts = productRepository.findAll();

        if (allProducts.isEmpty()) {
            return List.of();
        }

        try {
            // 2. Serialize products to JSON context for the LLM
            String inventoryJson = objectMapper.writeValueAsString(allProducts);

            // 3. Define System and User Prompt Template
            String promptMessage = """
                You are an intelligent product recommendation and scam-protection assistant for MatchGuard.
                
                Analyze the user query: "{userQuery}"
                Against the following active inventory items (JSON):
                {inventoryJson}
                
                Instructions:
                1. Match items that best satisfy the user's natural language query intent and budget.
                2. Calculate a compatibility 'fitScore' from 0 to 100 based on how well it matches.
                3. Write a brief, helpful 'compatibilityInsight' explaining why it's a good match.
                4. Strongly prioritize items where "isVerifiedSafe" is true or that have a high "trustScore".
                5. Return ONLY a valid JSON array (no markdown code blocks, no extra text) matching this exact schema:
                [
                  {
                    "productId": Long,
                    "title": String,
                    "description": String,
                    "price": Double,
                    "socialPostUrl": String,
                    "trustScore": Integer,
                    "isVerifiedSafe": Boolean,
                    "fitScore": Integer,
                    "compatibilityInsight": String
                  }
                ]
                """;

            PromptTemplate promptTemplate = new PromptTemplate(promptMessage);
            Prompt prompt = promptTemplate.create(Map.of(
                    "userQuery", userQuery,
                    "inventoryJson", inventoryJson
            ));

            // 4. Call OpenRouter GPT model via Spring AI
            ChatResponse response = chatModel.call(prompt);
//            String aiResponseContent = response.getResult().getOutput().getContent();
            String aiResponseContent = response.getResult().getOutput().getText();

            // 5. Clean up markdown wrappers if LLM includes ```json ... ```
            String cleanedJson = cleanJsonOutput(aiResponseContent);

            // 6. Parse JSON response into DTO list
            return objectMapper.readValue(cleanedJson, new TypeReference<List<ProductRecommendationDto>>() {});

        } catch (Exception e) {
            log.error("Failed to process AI conversational search via OpenRouter: {}", e.getMessage(), e);

            // Fallback: Return sorted stream fallback if LLM network or parsing hiccups occur
            return getFallbackRecommendations(allProducts, userQuery);
        }
    }

    private String cleanJsonOutput(String content) {
        if (content == null) return "[]";
        String trimmed = content.trim();
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.substring(7);
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.substring(3);
        }
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3);
        }
        return trimmed.trim();
    }

    private List<ProductRecommendationDto> getFallbackRecommendations(List<Product> products, String query) {
        return products.stream()
                .map(product -> ProductRecommendationDto.builder()
                        .productId(product.getId())
                        .title(product.getTitle())
                        .description(product.getDescription())
                        .price(product.getPrice())
                        .socialPostUrl(product.getSocialPostUrl())
                        .trustScore(product.getTrustScore() != null ? product.getTrustScore() : 80)
                        .isVerifiedSafe(product.getIsVerifiedSafe() != null ? product.getIsVerifiedSafe() : true)
                        .fitScore(75)
                        .compatibilityInsight("Fallback match for query: " + query)
                        .build())
                .collect(Collectors.toList());
    }
}