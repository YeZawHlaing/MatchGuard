package com.matchguard.backend.dto.paymentDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CancelTransactionRequestDto {

    @NotNull
    private Long transactionId;

    @NotBlank
    private String reason;
}
