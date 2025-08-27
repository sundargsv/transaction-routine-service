package com.poc.transaction.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {
    private Long transactionId;
    private Long accountId;
    private int operationTypeId;
    private BigDecimal amount;
    @JsonIgnore  // will not be serialized in API response, since this might be secure data
    private BigDecimal balance;
    private LocalDateTime eventDate;
}
