package com.poc.transaction.utils.mappers;

import com.poc.transaction.model.request.TransactionRequest;
import com.poc.transaction.model.response.TransactionResponse;
import com.poc.transaction.repository.entity.AccountEntity;
import com.poc.transaction.repository.entity.TransactionEntity;

import java.math.BigDecimal;

public class TransactionMapper {

    public static TransactionEntity toEntity(TransactionRequest request, AccountEntity account, BigDecimal signedAmount) {

        return TransactionEntity.builder()
                .account(account)
                .operationTypeId(request.getOperationTypeId())
                .amount(signedAmount)
                .eventDate(java.time.LocalDateTime.now())
                .status("COMPLETED") // Default status, can be changed later
                .build();
    }

    public static TransactionResponse toResponse(TransactionEntity entity) {
        return TransactionResponse.builder()
                .transactionId(entity.getTransactionId())
                .accountId(entity.getAccount().getAccountId())
                .operationTypeId(entity.getOperationTypeId())
                .amount(entity.getAmount())
                .eventDate(entity.getEventDate())
                .build();
    }
}
