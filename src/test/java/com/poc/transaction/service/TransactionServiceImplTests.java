package com.poc.transaction.service;

import com.poc.transaction.exception.ApiException;
import com.poc.transaction.model.request.TransactionRequest;
import com.poc.transaction.model.response.TransactionResponse;
import com.poc.transaction.repository.AccountRepository;
import com.poc.transaction.repository.TransactionRepository;
import com.poc.transaction.repository.entity.AccountEntity;
import com.poc.transaction.repository.entity.TransactionEntity;
import com.poc.transaction.service.transaction.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private AccountEntity account;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        account = AccountEntity.builder()
                .accountId(1L)
                .documentNumber("12345")
                .availableBalance(new BigDecimal("100.00"))
                .build();
    }

    @Test
    void testCreateTransaction_successfulPurchase() {
        TransactionRequest request = new TransactionRequest();
        request.setAccountId(1L);
        request.setOperationTypeId(1); // CASH PURCHASE (debt)
        request.setAmount(new BigDecimal("50.00"));

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(TransactionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        TransactionResponse response = transactionService.createTransaction(request);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getAccountId());
        verify(transactionRepository).save(any(TransactionEntity.class));
        verify(accountRepository).save(any(AccountEntity.class));

        // Assert - Balance should be reduced
        assertEquals(new BigDecimal("50.00"), account.getAvailableBalance());
    }

    @Test
    void testCreateTransaction_insufficientFunds() {
        TransactionRequest request = new TransactionRequest();
        request.setAccountId(1L);
        request.setOperationTypeId(3); // WITHDRAWAL
        request.setAmount(new BigDecimal("150.00"));

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        // Act
        ApiException ex = assertThrows(ApiException.class,
                () -> transactionService.createTransaction(request));

        // Assert
        assertEquals("INSUFFICIENT_FUNDS", ex.getCode());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus());

        verify(transactionRepository, never()).save(any());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void testCreateTransaction_paymentIncreasesBalance() {
        TransactionRequest request = new TransactionRequest();
        request.setAccountId(1L);
        request.setOperationTypeId(4); // PAYMENT (credit)
        request.setAmount(new BigDecimal("200.00"));

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(TransactionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        TransactionResponse response = transactionService.createTransaction(request);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getAccountId());
        assertEquals(new BigDecimal("300.00"), account.getAvailableBalance());

        verify(transactionRepository).save(any(TransactionEntity.class));
        verify(accountRepository).save(any(AccountEntity.class));
    }

    @Test
    void testCreateTransaction_invalidAccount() {
        TransactionRequest request = new TransactionRequest();
        request.setAccountId(99L);
        request.setOperationTypeId(1);
        request.setAmount(new BigDecimal("10.00"));

        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        ApiException ex = assertThrows(ApiException.class,
                () -> transactionService.createTransaction(request));

        // Assert
        assertEquals("ACCOUNT_NOT_FOUND", ex.getCode());
        assertEquals(HttpStatus.NOT_FOUND, ex.getHttpStatus());

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void testCreateTransaction_invalidOperationType() {
        TransactionRequest request = new TransactionRequest();
        request.setAccountId(1L);
        request.setOperationTypeId(99); // invalid
        request.setAmount(new BigDecimal("10.00"));

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        // Act
        ApiException ex = assertThrows(ApiException.class,
                () -> transactionService.createTransaction(request));

        // Assert
        assertEquals("INVALID_OPERATION_TYPE", ex.getCode());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus());

        verify(transactionRepository, never()).save(any());
        verify(accountRepository, never()).save(any());
    }
}

