package com.matchguard.backend.dto.productDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRecommendationDto {
    private Long productId;
    private String title;
    private String description;
    private Double price;
    private String socialPostUrl;
    private Integer trustScore;
    private Boolean isVerifiedSafe;
    private Integer fitScore;
    private String compatibilityInsight;  // Why it matches the user's search query
    private String explanation;           // Detailed breakdown of the product and its features
}