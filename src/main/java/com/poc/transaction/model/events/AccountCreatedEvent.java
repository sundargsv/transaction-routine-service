package com.poc.transaction.model.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// Domain Events for business processes (Notification, Balance Update which is adding more boilerplate code but Type-safe, clear contracts for other svc's
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountCreatedEvent {
    private long accountId;
    private String accountHolderName;
    private BigDecimal balance;
}
