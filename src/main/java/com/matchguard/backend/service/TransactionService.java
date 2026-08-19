package com.matchguard.backend.service;


import com.matchguard.backend.dto.paymentDto.CheckoutRequestDto;
import com.matchguard.backend.dto.paymentDto.CancelTransactionRequestDto;
import com.matchguard.backend.dto.paymentDto.ReleaseTransactionRequestDto;
import com.matchguard.backend.dto.paymentDto.TransactionResponseDto;
import com.matchguard.backend.entity.Product;
import com.matchguard.backend.entity.Transaction;
import com.matchguard.backend.entity.User;
import com.matchguard.backend.repository.ProductRepository;
import com.matchguard.backend.repository.TransactionRepository;
import com.matchguard.backend.repository.UserRepository;
import com.matchguard.backend.util.enums.TransactionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final R2StorageService r2StorageService;
    private final AiOcrService aiOcrService;
    private final QrCodeService qrCodeService;

    @Transactional
    public TransactionResponseDto processCheckout(CheckoutRequestDto request) {
        try {
            // 1. Upload screenshot file to Cloudflare R2 storage
            String screenshotUrl = r2StorageService.uploadFile(request.getScreenshot());

            // 2. Fetch managed User and Product entities
            User buyer = userRepository.findById(request.getBuyerId())
                    .orElseThrow(() -> new RuntimeException("Buyer not found with id: " + request.getBuyerId()));

            Product product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + request.getProductId()));

            // 3. Save initial transaction with PENDING_VERIFICATION status
            Transaction transaction = Transaction.builder()
                    .buyer(buyer)
                    .product(product)
                    .amount(request.getAmount())
                    .senderPhone(request.getSenderPhone())
                    .screenshotUrl(screenshotUrl)
                    .status(TransactionStatus.PENDING_VERIFICATION)
                    .build();

            transaction = transactionRepository.save(transaction);

            // 4. Trigger AI Vision OCR Verification using the R2 image URL
            AiOcrService.OcrVerificationResult aiResult = aiOcrService.verifyPaymentScreenshot(
                    screenshotUrl,
                    request.getAmount(),
                    request.getSenderPhone()
            );

            // 5. Advance status to ESCROW_LOCKED if verification passes
            if (aiResult.isVerified()) {
                transaction.setStatus(TransactionStatus.ESCROW_LOCKED);
                transaction.setQrToken(UUID.randomUUID().toString());
            }

            transaction = transactionRepository.save(transaction);

            return mapToDto(transaction, aiResult.reason());

        } catch (Exception e) {
            throw new RuntimeException("Checkout & R2 Upload failed: " + e.getMessage(), e);
        }
    }

    public List<TransactionResponseDto> getSellerTransactions(Long sellerId) {
        return transactionRepository.findByProduct_SellerIdOrderByUpdatedAtDesc(sellerId)
                .stream()
                .map(t -> mapToDto(t, "Verified order in escrow"))
                .collect(Collectors.toList());
    }

    private TransactionResponseDto mapToDto(Transaction t, String notes) {
        return TransactionResponseDto.builder()
                .id(t.getId())
                .productId(t.getProduct() != null ? t.getProduct().getId() : null)
                .productTitle(t.getProduct() != null ? t.getProduct().getTitle() : null)
                .amount(t.getAmount())
                .status(t.getStatus())
                .senderPhone(t.getSenderPhone())
                .screenshotUrl(t.getScreenshotUrl())
                .qrToken(t.getQrToken())
                .aiVerificationNotes(notes)
                .updatedAt(t.getUpdatedAt())
                .build();
    }


    @Transactional
    public TransactionResponseDto manualApproveBySeller(Long transactionId, Long sellerId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + transactionId));

        // Verify that the product belongs to this seller for security
        if (transaction.getProduct() == null || !transaction.getProduct().getSeller().getId().equals(sellerId)) {
            throw new RuntimeException("Unauthorized: You do not own this product's transaction.");
        }

        // Advance status to ESCROW_LOCKED and generate QR Token if it was pending
        if (transaction.getStatus() == TransactionStatus.PENDING_VERIFICATION) {
            transaction.setStatus(TransactionStatus.ESCROW_LOCKED);
            if (transaction.getQrToken() == null) {
                transaction.setQrToken(UUID.randomUUID().toString());
            }
            transaction = transactionRepository.save(transaction);
        }

        return mapToDto(transaction, "Manually verified and accepted by seller.");
    }

    @Transactional(readOnly = true)
    public byte[] generateQrCode(Long transactionId, String sellerEmail) {
        Transaction transaction = getTransaction(transactionId);
        ensureSellerOwnsTransaction(transaction, sellerEmail);

        if (transaction.getStatus() != TransactionStatus.ESCROW_LOCKED || transaction.getQrToken() == null) {
            throw new IllegalStateException("A QR code is available only for escrow-locked transactions.");
        }

        return qrCodeService.generatePng(transaction.getId() + ":" + transaction.getQrToken());
    }

    @Transactional
    public TransactionResponseDto releaseTransaction(ReleaseTransactionRequestDto request, String actorEmail) {
        Transaction transaction = getTransaction(request.getTransactionId());
        ensureParticipant(transaction, actorEmail);

        if (transaction.getStatus() != TransactionStatus.ESCROW_LOCKED) {
            throw new IllegalStateException("Only escrow-locked transactions can be completed.");
        }
        if (!request.getQrToken().equals(transaction.getQrToken())) {
            throw new IllegalArgumentException("Invalid QR token.");
        }

        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction = transactionRepository.save(transaction);
        log.info("Payout intent logged for transaction {} to seller phone {}", transaction.getId(), transaction.getProduct().getSeller().getPhone());
        return mapToDto(transaction, "QR handover verified. Payout intent logged.");
    }

    @Transactional
    public TransactionResponseDto cancelTransaction(CancelTransactionRequestDto request, String buyerEmail) {
        Transaction transaction = getTransaction(request.getTransactionId());

        if (!transaction.getBuyer().getEmail().equalsIgnoreCase(buyerEmail)) {
            throw new AccessDeniedException("Only the buyer who created the transaction can request a cancellation.");
        }
        if (transaction.getStatus() != TransactionStatus.ESCROW_LOCKED && transaction.getStatus() != TransactionStatus.PENDING_VERIFICATION) {
            throw new IllegalStateException("This transaction can no longer be cancelled.");
        }

        transaction.setStatus(TransactionStatus.CANCELLED_AND_REFUNDED);
        transaction = transactionRepository.save(transaction);
        log.info("Cancellation and refund intent logged for transaction {}. Reason: {}", transaction.getId(), request.getReason());
        return mapToDto(transaction, "Cancellation requested: " + request.getReason());
    }

    private Transaction getTransaction(Long transactionId) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + transactionId));
    }

    private void ensureSellerOwnsTransaction(Transaction transaction, String sellerEmail) {
        if (!transaction.getProduct().getSeller().getEmail().equalsIgnoreCase(sellerEmail)) {
            throw new AccessDeniedException("Only the seller who owns this transaction can generate its QR code.");
        }
    }

    private void ensureParticipant(Transaction transaction, String actorEmail) {
        boolean isBuyer = transaction.getBuyer().getEmail().equalsIgnoreCase(actorEmail);
        boolean isSeller = transaction.getProduct().getSeller().getEmail().equalsIgnoreCase(actorEmail);
        if (!isBuyer && !isSeller) {
            throw new AccessDeniedException("Only the buyer or seller can complete this transaction.");
        }
    }
}
