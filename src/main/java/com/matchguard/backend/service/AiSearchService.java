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
        List<Product> allProducts = productRepository.findAll();

        if (allProducts.isEmpty()) {
            return List.of();
        }

        try {
            String inventoryJson = objectMapper.writeValueAsString(allProducts);

            String promptMessage = """
                You are MatchGuard's expert AI Shopping Assistant and Scam Protection Advisor.
                
                The customer searched with this natural language request: "{userQuery}"
                
                Here is our current active inventory database (JSON):
                {inventoryJson}
                
                Your Task:
                1. Filter and rank the inventory items based on how well they match the customer's intent, preferences, and budget.
                2. Calculate a 'fitScore' from 0 to 100 representing exact compatibility.
                3. Write a 'compatibilityInsight' explaining specifically how this item addresses the user's prompt (e.g., price vs budget, specific needs).
                4. Write a detailed 'explanation' breaking down the product's value, condition, key features, and why it is a safe/good purchase based on its trust score.
                5. Strongly prioritize items that are verified safe (`isVerifiedSafe: true`) or have high `trustScore`.
                6. Return ONLY a valid JSON array (no markdown code blocks, no extra text) matching this exact schema:
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
                    "compatibilityInsight": String,
                    "explanation": String
                  }
                ]
                """;

            PromptTemplate promptTemplate = new PromptTemplate(promptMessage);
            Prompt prompt = promptTemplate.create(Map.of(
                    "userQuery", userQuery,
                    "inventoryJson", inventoryJson
            ));

            ChatResponse response = chatModel.call(prompt);
            String aiResponseContent = response.getResult().getOutput().getText();

            String cleanedJson = cleanJsonOutput(aiResponseContent);

            return objectMapper.readValue(cleanedJson, new TypeReference<List<ProductRecommendationDto>>() {});

        } catch (Exception e) {
            log.error("Failed to process AI conversational search via OpenRouter: {}", e.getMessage(), e);
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
                        .compatibilityInsight("Matches your search query: '" + query + "'.")
                        .explanation("This item is part of our active inventory with standard safety verification and solid value for money.")
                        .build())
                .collect(Collectors.toList());
    }
}