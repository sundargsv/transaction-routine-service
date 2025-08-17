package com.poc.transaction.handlers;

import com.poc.transaction.model.events.AccountCreatedEvent;
import com.poc.transaction.model.events.GenericEventMessage;
import com.poc.transaction.repository.entity.AccountEntity;
import com.poc.transaction.service.cache.CacheService;
import com.poc.transaction.service.eventBus.EventBusService;
import com.poc.transaction.utils.mappers.AccountMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.poc.transaction.utils.CommonUtils.prepareEvent;
import static com.poc.transaction.utils.Constants.CACHE_PREFIX;
import static com.poc.transaction.utils.Constants.KafkaTopics.AUDIT_LOG;
import static com.poc.transaction.utils.Constants.KafkaTopics.NOTIFICATION;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncEventDispatcher {

    private final CacheService cacheService;
    private final EventBusService eventBusService;

    // Optionally cache the newly created account - Write-Through & TTL Asynchronously
    // TODO: Publish Kafka audit event ACCOUNT_CREATED - Asynchronously
    @Async
    public void handlePostAccountCreation(AccountEntity account) {
        String cacheKey = CACHE_PREFIX + account.getAccountId();

        try {
            // 1. Update Redis cache with TTL (Write-Through Cache to avoid stale data)
            cacheService.set(cacheKey, AccountMapper.toResponse(account));
            log.info("Account cached in Redis: {}", account.getAccountId());

            // 2. Publish Notification Event
            publishAccountCreatedNotification(account);

            // 3. Publish Audit Event
            publishAccountCreatedAudit(account);

        } catch (Exception e) {
            log.error("Error in async event dispatch for account {}", account.getAccountId(), e);
        }
    }

    private void publishAccountCreatedNotification(AccountEntity account) {

        //prepare strongly-typed event for notification
        AccountCreatedEvent domainEvent = AccountCreatedEvent.builder()
                .accountId(account.getAccountId())
                .accountHolderName("")
                .balance(account.getAvailableBalance())
                .build();

        //emit event
        eventBusService.publishEvent(NOTIFICATION, prepareEvent(domainEvent, "NOTIFY_ACCOUNT_CREATED_SUCCESS"));
    }

    private void publishAccountCreatedAudit(AccountEntity account) {
        //prepare generic event for audit logging
        GenericEventMessage auditEvent = prepareEvent(
                Map.of(
                        "accountId", account.getAccountId(),
                        "documentNumber", account.getDocumentNumber(),
                        "balance", account.getAvailableBalance()
                ),
                "ACCOUNT_CREATED"
        );

        //emit event
        eventBusService.publishEvent(AUDIT_LOG, prepareEvent(auditEvent, "ACCOUNT_CREATED"));
    }
}