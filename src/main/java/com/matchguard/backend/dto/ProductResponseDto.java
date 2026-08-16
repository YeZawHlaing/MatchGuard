package com.matchguard.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponseDto {
    private Long id;
    private Long sellerId;
    private String title;
    private String description;
    private Double price;
    private String socialPostUrl;
    private Integer trustScore;
    private String scamAnalysisSummary;
    private Boolean isVerifiedSafe;
    private LocalDateTime createdAt;
}
