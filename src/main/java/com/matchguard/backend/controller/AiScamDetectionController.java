package com.matchguard.backend.controller;

import com.matchguard.backend.dto.AiScamDetectionResult;
import com.matchguard.backend.dto.ProductRequestDto;
import com.matchguard.backend.service.AiScamDetectionService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiScamDetectionController {

    private final AiScamDetectionService aiScamDetectionService;

    public AiScamDetectionController(
            AiScamDetectionService aiScamDetectionService
    ) {
        this.aiScamDetectionService = aiScamDetectionService;
    }

    @PostMapping("/scam-detection")
    public AiScamDetectionResult analyzeProduct(
            @RequestBody ProductRequestDto request
    ) {
        return aiScamDetectionService.analyzeProduct(
                request.getTitle(),
                request.getDescription(),
                request.getPrice(),
                request.getSocialPostUrl()
        );
    }
}