package com.matchguard.backend.service;

import com.matchguard.backend.dto.paymentDto.CancelTransactionRequestDto;
import com.matchguard.backend.dto.paymentDto.ReleaseTransactionRequestDto;
import com.matchguard.backend.dto.paymentDto.TransactionResponseDto;
import com.matchguard.backend.entity.Product;
import com.matchguard.backend.entity.Transaction;
import com.matchguard.backend.entity.User;
import com.matchguard.backend.repository.ProductRepository;
import com.matchguard.backend.repository.TransactionRepository;
import com.matchguard.backend.repository.UserRepository;
import com.matchguard.backend.util.enums.Role;
import com.matchguard.backend.util.enums.TransactionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProductRepository productRepository;
    @Mock private R2StorageService r2StorageService;
    @Mock private AiOcrService aiOcrService;

    private TransactionService transactionService;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(
                transactionRepository, userRepository, productRepository, r2StorageService, aiOcrService, new QrCodeService());

        User seller = User.builder().id(2L).email("seller@example.com").phone("09900000000").role(Role.SELLER).build();
        User buyer = User.builder().id(1L).email("buyer@example.com").role(Role.CUSTOMER).build();
        Product product = Product.builder().id(10L).seller(seller).title("Phone").build();
        transaction = Transaction.builder()
                .id(22L).buyer(buyer).product(product).status(TransactionStatus.ESCROW_LOCKED)
                .qrToken("one-time-token").build();
    }

    @Test
    void sellerCanGeneratePngQrCodeForLockedTransaction() {
        when(transactionRepository.findById(22L)).thenReturn(Optional.of(transaction));

        byte[] qrCode = transactionService.generateQrCode(22L, "seller@example.com");

        assertTrue(qrCode.length > 8);
        assertEquals((byte) 0x89, qrCode[0]);
    }

    @Test
    void validParticipantQrTokenCompletesTransaction() {
        when(transactionRepository.findById(22L)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ReleaseTransactionRequestDto request = new ReleaseTransactionRequestDto();
        request.setTransactionId(22L);
        request.setQrToken("one-time-token");

        TransactionResponseDto response = transactionService.releaseTransaction(request, "buyer@example.com");

        assertEquals(TransactionStatus.COMPLETED, response.getStatus());
        verify(transactionRepository).save(transaction);
    }

    @Test
    void invalidQrTokenDoesNotCompleteTransaction() {
        when(transactionRepository.findById(22L)).thenReturn(Optional.of(transaction));
        ReleaseTransactionRequestDto request = new ReleaseTransactionRequestDto();
        request.setTransactionId(22L);
        request.setQrToken("wrong-token");

        assertThrows(IllegalArgumentException.class,
                () -> transactionService.releaseTransaction(request, "buyer@example.com"));
        assertEquals(TransactionStatus.ESCROW_LOCKED, transaction.getStatus());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void buyerCanCancelBeforeHandover() {
        when(transactionRepository.findById(22L)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        CancelTransactionRequestDto request = new CancelTransactionRequestDto();
        request.setTransactionId(22L);
        request.setReason("Item was not as described");

        TransactionResponseDto response = transactionService.cancelTransaction(request, "buyer@example.com");

        assertEquals(TransactionStatus.CANCELLED_AND_REFUNDED, response.getStatus());
        verify(transactionRepository).save(transaction);
    }
}
