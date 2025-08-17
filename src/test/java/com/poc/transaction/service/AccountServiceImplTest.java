package com.poc.transaction.service;

import static com.poc.transaction.utils.Constants.CACHE_PREFIX;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.poc.transaction.exception.ApiException;
import com.poc.transaction.handlers.AsyncEventDispatcher;
import com.poc.transaction.repository.entity.AccountEntity;
import com.poc.transaction.model.request.AccountRequest;
import com.poc.transaction.model.response.AccountResponse;
import com.poc.transaction.repository.AccountRepository;
import com.poc.transaction.service.account.AccountServiceImpl;
import com.poc.transaction.service.cache.CacheService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.util.Optional;

class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CacheService cacheService;

    @Mock
    private AsyncEventDispatcher asyncEventDispatcher;

    @InjectMocks
    private AccountServiceImpl accountService;

    @BeforeEach
    void setUp() {
        // set all mocks
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createAccount_success() {
        AccountRequest request = new AccountRequest("123456789");
        when(accountRepository.findByDocumentNumber(request.getDocumentNumber()))
                .thenReturn(Optional.empty());

        AccountEntity savedEntity = AccountEntity.builder()
                .accountId(1L)
                .documentNumber(request.getDocumentNumber())
                .availableBalance(java.math.BigDecimal.ZERO)
                .build();

        when(accountRepository.save(any(AccountEntity.class)))
                .thenReturn(savedEntity);

        // Act
        AccountResponse response = accountService.createAccount(request);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getAccountId());
        assertEquals("123456789", response.getDocumentNumber());
        verify(accountRepository).save(any(AccountEntity.class));
    }

    @Test
    void createAccount_duplicateDocument_throwsException() {
        AccountRequest request = new AccountRequest("123456789");
        AccountEntity existing = AccountEntity.builder()
                .accountId(1L)
                .documentNumber("123456789")
                .build();

        when(accountRepository.findByDocumentNumber(request.getDocumentNumber()))
                .thenReturn(Optional.of(existing));

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class,
                () -> accountService.createAccount(request));

        assertEquals("ACCOUNT_EXISTS", exception.getCode());
        assertEquals(HttpStatus.CONFLICT, exception.getHttpStatus());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void getAccount_foundInCache() {
        Long accountId = 1L;
        AccountResponse cachedResponse = new AccountResponse(1L, "123456789");
        when(cacheService.get(CACHE_PREFIX + accountId))
                .thenReturn(cachedResponse);

        // Act
        AccountResponse response = accountService.getAccount(accountId);

        // Assert
        assertEquals(cachedResponse, response);
        verify(accountRepository, never()).findById(accountId);
    }

    @Test
    void getAccount_notFound_throwsException() {
        Long accountId = 1L;
        when(cacheService.get(CACHE_PREFIX + accountId)).thenReturn(null);
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class,
                () -> accountService.getAccount(accountId));

        assertEquals("ACCOUNT_NOT_FOUND", exception.getCode());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
    }

    @Test
    void getAccount_fromDbAndCacheIt() {

        Long accountId = 1L;
        when(cacheService.get(CACHE_PREFIX + accountId)).thenReturn(null);
        // stubbing
        doNothing().when(cacheService).set(eq(CACHE_PREFIX + accountId), any(AccountResponse.class));

        AccountEntity entity = AccountEntity.builder()
                .accountId(1L)
                .documentNumber("123456789")
                .availableBalance(java.math.BigDecimal.ZERO)
                .build();

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(entity));

        // Act
        AccountResponse response = accountService.getAccount(accountId);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getAccountId());
        assertEquals("123456789", response.getDocumentNumber());

        verify(cacheService).set(eq(CACHE_PREFIX + accountId), any(AccountResponse.class));
    }
}
