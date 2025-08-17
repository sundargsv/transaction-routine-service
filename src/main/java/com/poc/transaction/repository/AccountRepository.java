package com.poc.transaction.repository;

import com.poc.transaction.repository.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<AccountEntity, Long> {
    Optional<AccountEntity> findByDocumentNumber(String documentNumber);
}
