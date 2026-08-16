package com.matchguard.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequestDto {
    private Long sellerId;
    private String title;
    private String description;
    private Double price;
    private String socialPostUrl;
}
