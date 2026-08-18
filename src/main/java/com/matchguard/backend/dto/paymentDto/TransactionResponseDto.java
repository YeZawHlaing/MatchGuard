package com.matchguard.backend.dto.paymentDto;


import com.matchguard.backend.util.enums.TransactionStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionResponseDto {
    private Long id;
    private Long productId;
    private String productTitle;
    private Double amount;
    private TransactionStatus status;
    private String senderPhone;
    private String screenshotUrl;
    private String qrToken;
    private String aiVerificationNotes; // Passed in response from OCR result
    private LocalDateTime updatedAt;
}