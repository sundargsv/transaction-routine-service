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
import java.time.LocalDateTime;
import java.util.List;
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
        // verify(accountRepository).save(any(AccountEntity.class));

        // Assert - not updating account's balance for now
        // assertEquals(new BigDecimal("50.00"), account.getAvailableBalance());
    }

    // Payment offsets multiple purchases fully → leftover becomes 0.
    @Test
    void testDischarge_paymentOffsetsMultiplePurchases() {
        // Given account
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        // Mock previous purchases with negative balances
        TransactionEntity purchase1 = TransactionEntity.builder()
                .transactionId(101L)
                .account(account)
                .operationTypeId(1) // CASH PURCHASE
                .amount(new BigDecimal("-50.00"))
                .balance(new BigDecimal("-50.00"))
                .eventDate(LocalDateTime.now().minusDays(2))
                .build();

        TransactionEntity purchase2 = TransactionEntity.builder()
                .transactionId(102L)
                .account(account)
                .operationTypeId(2) // INSTALLMENT PURCHASE
                .amount(new BigDecimal("-10.00"))
                .balance(new BigDecimal("-10.00"))
                .eventDate(LocalDateTime.now().minusDays(1).minusHours(6))
                .build();

        TransactionEntity purchase3 = TransactionEntity.builder()
                .transactionId(103L)
                .account(account)
                .operationTypeId(3) // WITHDRAWAL
                .amount(new BigDecimal("-10.00"))
                .balance(new BigDecimal("-10.00"))
                .eventDate(LocalDateTime.now().minusDays(1).minusHours(2))
                .build();

        // Payment request
        TransactionRequest paymentRequest = new TransactionRequest();
        paymentRequest.setAccountId(1L);
        paymentRequest.setOperationTypeId(4);
        paymentRequest.setAmount(new BigDecimal("50.00"));

        TransactionEntity savedPayment = TransactionEntity.builder()
                .transactionId(200L)
                .account(account)
                .operationTypeId(4)
                .amount(new BigDecimal("50.00"))
                .balance(null)
                .build();

        when(transactionRepository.save(any(TransactionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(transactionRepository.fetchTransactions(eq(1L), any()))
                .thenReturn(List.of(purchase1, purchase2, purchase3));

        // When
        TransactionResponse response = transactionService.createTransaction(paymentRequest);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getAccountId());

        // Verify balances were updated correctly:
        // Payment 50 offsets purchase1 (-50) fully -> purchase1 = 0
        // Remaining 0 left, so purchase2 & purchase3 unchanged
        assertTrue(BigDecimal.ZERO.compareTo(purchase1.getBalance()) == 0);
        assertEquals(new BigDecimal("-10.00"), purchase2.getBalance());
        assertEquals(new BigDecimal("-10.00"), purchase3.getBalance());
    }

    // Payment partially offsets purchase → purchase still negative with decimals (-9.50), payment exhausted.
    @Test
    void testDischarge_paymentPartiallyOffsetsPurchases_withDecimals() {
        // Given account
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        // Mock previous purchases with negative balances
        TransactionEntity purchase1 = TransactionEntity.builder()
                .transactionId(101L)
                .account(account)
                .operationTypeId(1) // CASH PURCHASE
                .amount(new BigDecimal("-50.00"))
                .balance(new BigDecimal("-50.00"))
                .eventDate(LocalDateTime.now().minusDays(2))
                .build();

        TransactionEntity purchase2 = TransactionEntity.builder()
                .transactionId(102L)
                .account(account)
                .operationTypeId(2) // INSTALLMENT PURCHASE
                .amount(new BigDecimal("-10.00"))
                .balance(new BigDecimal("-10.00"))
                .eventDate(LocalDateTime.now().minusDays(1).minusHours(6))
                .build();

        // Payment request
        TransactionRequest paymentRequest = new TransactionRequest();
        paymentRequest.setAccountId(1L);
        paymentRequest.setOperationTypeId(4);
        paymentRequest.setAmount(new BigDecimal("40.50"));

        TransactionEntity savedPayment = TransactionEntity.builder()
                .transactionId(200L)
                .account(account)
                .operationTypeId(4)
                .amount(new BigDecimal("40.50"))
                .balance(null)
                .build();

        when(transactionRepository.save(any(TransactionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(transactionRepository.fetchTransactions(eq(1L), any()))
                .thenReturn(List.of(purchase1, purchase2));

        // When
        TransactionResponse response = transactionService.createTransaction(paymentRequest);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getAccountId());

        // Verify balances were updated correctly:
        // Payment 50 offsets purchase1 (-40.50) fully -> purchase1 = -9.50
        // Remaining 0 left, so purchase2 unchanged
        assertEquals(new BigDecimal("-9.50"), purchase1.getBalance());
        assertEquals(new BigDecimal("-10.00"), purchase2.getBalance());
    }


    // Payment partially offsets purchase → purchase still negative, payment exhausted.
    @Test
    void testDischarge_paymentPartiallyOffsetsPurchases() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        TransactionEntity purchase1 = TransactionEntity.builder()
                .transactionId(201L)
                .account(account)
                .operationTypeId(1)
                .amount(new BigDecimal("-50.00"))
                .balance(new BigDecimal("-50.00"))
                .eventDate(LocalDateTime.now().minusDays(1).minusHours(6))
                .build();

        TransactionRequest paymentRequest = new TransactionRequest();
        paymentRequest.setAccountId(1L);
        paymentRequest.setOperationTypeId(4); // PAYMENT
        paymentRequest.setAmount(new BigDecimal("20.00"));

        when(transactionRepository.save(any(TransactionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(transactionRepository.fetchTransactions(eq(1L), any()))
                .thenReturn(List.of(purchase1));

        TransactionResponse response = transactionService.createTransaction(paymentRequest);

        // 20 offset -> purchase1 = -30, payment left = 0
        assertEquals(new BigDecimal("-30.00"), purchase1.getBalance());
        assertEquals(0, response.getBalance().compareTo(BigDecimal.ZERO));

    }

    // Payment greater than all purchases → leftover balance remains positive in payment.
    @Test
    void testDischarge_paymentGreaterThanAllPurchases() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        TransactionEntity purchase1 = TransactionEntity.builder()
                .transactionId(301L)
                .account(account)
                .operationTypeId(1)
                .amount(new BigDecimal("-10.00"))
                .balance(new BigDecimal("-10.00"))
                .eventDate(LocalDateTime.now().minusDays(1).minusHours(6))
                .build();

        TransactionRequest paymentRequest = new TransactionRequest();
        paymentRequest.setAccountId(1L);
        paymentRequest.setOperationTypeId(4); // PAYMENT
        paymentRequest.setAmount(new BigDecimal("50.00"));

        when(transactionRepository.save(any(TransactionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(transactionRepository.fetchTransactions(eq(1L), any()))
                .thenReturn(List.of(purchase1));

        TransactionResponse response = transactionService.createTransaction(paymentRequest);

        // purchase1 fully offset -> 0
        // assertEquals(BigDecimal.ZERO, purchase1.getBalance());
        assertEquals(0, purchase1.getBalance().compareTo(BigDecimal.ZERO));
        // payment leftover = 40
        assertEquals(new BigDecimal("40.00"), response.getBalance());
    }

    // No purchases exist → payment balance unchanged.
    @Test
    void testDischarge_noPurchasesFound() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        TransactionRequest paymentRequest = new TransactionRequest();
        paymentRequest.setAccountId(1L);
        paymentRequest.setOperationTypeId(4);
        paymentRequest.setAmount(new BigDecimal("25.00"));

        when(transactionRepository.save(any(TransactionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(transactionRepository.fetchTransactions(eq(1L), any()))
                .thenReturn(List.of()); // no purchases

        TransactionResponse response = transactionService.createTransaction(paymentRequest);

        // No purchases to offset -> payment balance stays same
        assertEquals(new BigDecimal("25.00"), response.getBalance());
    }


    // @Test - removing this now cause not checking the account balance here
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

    // @Test - removing this now because not adding the account balance here
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

