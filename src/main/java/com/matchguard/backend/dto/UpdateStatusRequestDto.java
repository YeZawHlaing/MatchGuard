package com.matchguard.backend.dto;

import com.matchguard.backend.util.enums.TransactionStatus;
import lombok.Data;

@Data
public class UpdateStatusRequestDto {
    private TransactionStatus status;
}
