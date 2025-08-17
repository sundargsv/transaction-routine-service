package com.poc.transaction.handlers;

import com.poc.transaction.repository.entity.AccountEntity;
import com.poc.transaction.service.cache.CacheService;
import com.poc.transaction.utils.mappers.AccountMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import static com.poc.transaction.utils.Constants.CACHE_PREFIX;
@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncEventDispatcher {

    private final CacheService cacheService;


    // Optionally cache the newly created account - Write-Through & TTL Asynchronously
    // TODO: Publish Kafka audit event ACCOUNT_CREATED - Asynchronously
    @Async
    public void handlePostAccountCreation(AccountEntity account) {
        String cacheKey = CACHE_PREFIX + account.getAccountId();

        try {
            // 1. Update Redis cache with TTL (Write-Through Cache to avoid stale data)
            cacheService.set(cacheKey, AccountMapper.toResponse(account));
            log.info("Account cached in Redis: {}", account.getAccountId());

        } catch (Exception e) {
            log.error("Error in async event dispatch for account {}", account.getAccountId(), e);
        }
    }
}
