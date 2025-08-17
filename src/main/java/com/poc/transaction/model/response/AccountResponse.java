package com.poc.transaction.model.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountResponse {
    private Long accountId;
    private String documentNumber;
    // availablebalance intentionally hidden in API response
}
