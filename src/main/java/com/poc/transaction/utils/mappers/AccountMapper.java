package com.poc.transaction.utils.mappers;

import com.poc.transaction.model.request.AccountRequest;
import com.poc.transaction.model.response.AccountResponse;
import com.poc.transaction.repository.entity.AccountEntity;

public class AccountMapper {

    public static AccountEntity toEntity(AccountRequest request) {
        return AccountEntity.builder()
                .documentNumber(request.getDocumentNumber())
                .availableBalance(java.math.BigDecimal.ZERO)
                .build();
    }

    public static AccountResponse toResponse(AccountEntity entity) {
        return AccountResponse.builder()
                .accountId(entity.getAccountId())
                .documentNumber(entity.getDocumentNumber())
                .build();
    }
}

