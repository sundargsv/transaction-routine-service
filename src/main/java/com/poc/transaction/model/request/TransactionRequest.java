package com.poc.transaction.model.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionRequest {
    @NotNull(message = "Account Id must be provided")
    private Long accountId;

    @NotNull(message = "Operation Type Id must be provided")
    private int operationTypeId;

    @NotNull(message = "Amount must be provided")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be positive and greater than zero")
    private BigDecimal amount;
}
