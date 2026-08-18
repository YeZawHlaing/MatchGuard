package com.matchguard.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import java.net.URI;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiOcrService {

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;

    public OcrVerificationResult verifyPaymentScreenshot(String imageUrl, Double expectedAmount, String expectedPhone) {
        try {
            String instructions = String.format("""
                You are a banking OCR verification assistant. 
                Look at the provided payment transfer screenshot.
                Check if the transferred amount is exactly %.2f and if the sender/receiver phone involves '%s'.
                Return strictly a JSON object with this format:
                {
                   "isVerified": true/false,
                   "reason": "Brief explanation of what you found on the receipt"
                }
                """, expectedAmount, expectedPhone);

            // Use UserMessage.builder() for Spring AI 2.0.0
            UserMessage userMessage = UserMessage.builder()
                    .text(instructions)
                    .media(new Media(MimeTypeUtils.IMAGE_JPEG, URI.create(imageUrl)))
                    .build();

            ChatResponse response = chatModel.call(new Prompt(userMessage));
            String jsonContent = cleanJsonOutput(response.getResult().getOutput().getText());

            JsonNode rootNode = objectMapper.readTree(jsonContent);
            return new OcrVerificationResult(
                    rootNode.get("isVerified").asBoolean(),
                    rootNode.get("reason").asText()
            );

        } catch (Exception e) {
            log.error("AI OCR Verification failed: {}", e.getMessage());
            return new OcrVerificationResult(false, "System failed to read the receipt image automatically. Manual review required.");
        }
    }

    private String cleanJsonOutput(String content) {
        if (content == null) return "{}";
        String trimmed = content.trim();
        if (trimmed.startsWith("```json")) trimmed = trimmed.substring(7);
        else if (trimmed.startsWith("```")) trimmed = trimmed.substring(3);
        if (trimmed.endsWith("```")) trimmed = trimmed.substring(0, trimmed.length() - 3);
        return trimmed.trim();
    }

    public record OcrVerificationResult(boolean isVerified, String reason) {}
}