package com.poc.transaction.repository;

import com.poc.transaction.repository.entity.AccountEntity;
import com.poc.transaction.repository.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {
    // Option 1: Query using AccountEntity object
    List<TransactionEntity> findByAccount(AccountEntity account);

    // Option 2: Query directly using accountId
    List<TransactionEntity> findByAccount_AccountId(Long accountId);
}
