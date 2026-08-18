package com.matchguard.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiScamDetectionResult {

    private Integer trustScore;

    private String scamAnalysisSummary;

    private Boolean isVerifiedSafe;
}