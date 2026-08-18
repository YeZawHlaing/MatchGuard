package com.matchguard.backend.dto.paymentDto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CheckoutRequestDto {
    private Long productId;
    private Long buyerId;
    private Double amount;
    private String senderPhone;
    private MultipartFile screenshot; // Actual image file uploaded by customer
}
