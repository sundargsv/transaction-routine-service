package com.poc.transaction.service.account;

import com.poc.transaction.exception.ApiException;
import com.poc.transaction.handlers.AsyncEventDispatcher;
import com.poc.transaction.model.request.AccountRequest;
import com.poc.transaction.model.response.AccountResponse;
import com.poc.transaction.repository.AccountRepository;
import com.poc.transaction.repository.entity.AccountEntity;
import com.poc.transaction.service.cache.CacheService;
import com.poc.transaction.utils.mappers.AccountMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.poc.transaction.utils.Constants.CACHE_PREFIX;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AsyncEventDispatcher asyncEventDispatcher;
    private final CacheService cacheService;


    public AccountResponse createAccount(AccountRequest request) {
        accountRepository.findByDocumentNumber(request.getDocumentNumber())
                .ifPresent(existing -> {
                    throw new ApiException("Account with this document already exists",
                            HttpStatus.CONFLICT, "ACCOUNT_EXISTS");
                });

        AccountEntity entity = AccountMapper.toEntity(request);
        AccountEntity saved = accountRepository.save(entity);

        // Fire async process for cache (doesn't block API response or may emit even before db update which is okay in this case) fire-and-forget (ec)
        asyncEventDispatcher.handlePostAccountCreation(saved);

        return AccountMapper.toResponse(saved);
    }

    public AccountResponse getAccount(Long accountId) {
        String cacheKey = CACHE_PREFIX + accountId;

        // Try cache first
        Object cached = cacheService.get(cacheKey);
        if (cached instanceof AccountResponse) {
            return (AccountResponse) cached;
        }

        // DB lookup
        Optional<AccountEntity> entityOpt = accountRepository.findById(accountId);

        //throw not found exception if not present
        if (entityOpt.isEmpty()) {
            throw new ApiException("Account not found", HttpStatus.NOT_FOUND, "ACCOUNT_NOT_FOUND");
        }

        AccountResponse response = AccountMapper.toResponse(entityOpt.get());

        // Cache result with TTL (avoid stale data)
        // Asynchronously cache the response
        cacheService.set(cacheKey, response);

        return response;
    }
}
