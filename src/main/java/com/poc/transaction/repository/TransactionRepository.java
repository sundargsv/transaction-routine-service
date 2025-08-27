package com.poc.transaction.repository;

import com.poc.transaction.repository.entity.AccountEntity;
import com.poc.transaction.repository.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {
    // Option 1: Query using AccountEntity object
    List<TransactionEntity> findByAccount(AccountEntity account);

    // Option 2: Query directly using accountId
    List<TransactionEntity> findByAccount_AccountId(Long accountId);

    @Query(" SELECT t FROM TransactionEntity t WHERE t.account.accountId = :accountId AND t.operationTypeId IN (1, 2, 3) AND t.balance != 0 AND t.eventDate <= :currentDateTime")
    List<TransactionEntity> fetchTransactions(Long accountId, LocalDateTime currentDateTime);
}
