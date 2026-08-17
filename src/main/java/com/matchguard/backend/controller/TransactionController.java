package com.matchguard.backend.controller;


import com.matchguard.backend.dto.paymentDto.CheckoutRequestDto;
import com.matchguard.backend.dto.paymentDto.TransactionResponseDto;
import com.matchguard.backend.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    // CUSTOMER checks out, uploads receipt file directly to R2, and triggers AI OCR
    @PostMapping(value = "/checkout", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<TransactionResponseDto> checkout(
            @ModelAttribute CheckoutRequestDto request
    ) {
        TransactionResponseDto response = transactionService.processCheckout(request);
        return ResponseEntity.ok(response);
    }

    // SELLER views their secured orders dashboard
    @GetMapping("/seller/{sellerId}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<List<TransactionResponseDto>> getSellerDashboard(@PathVariable Long sellerId) {
        List<TransactionResponseDto> transactions = transactionService.getSellerTransactions(sellerId);
        return ResponseEntity.ok(transactions);
    }

    // SELLER manually approves a pending transaction
    @PostMapping("/{transactionId}/approve/{sellerId}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<TransactionResponseDto> approveTransaction(
            @PathVariable Long transactionId,
            @PathVariable Long sellerId
    ) {
        TransactionResponseDto response = transactionService.manualApproveBySeller(transactionId, sellerId);
        return ResponseEntity.ok(response);
    }
}
