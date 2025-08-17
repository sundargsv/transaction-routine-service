package com.poc.transaction.consumers;

import com.poc.transaction.model.events.AccountCreatedEvent;
import com.poc.transaction.model.events.GenericEventMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import static com.poc.transaction.utils.Constants.KafkaTopics.NOTIFICATION;

@Slf4j
@Service
public class NotificationConsumer {

    @KafkaListener(
            topics = NOTIFICATION, // audit log topic
            groupId = "consumer", // no consumer group id since its running in only one instance
            containerFactory = "kafkaListenerContainerFactory"
    )
    public <T> void consume(GenericEventMessage<T> message) {
        log.info("NOTIFICATION-SERVICE-MOCK: Received notification event: eventId={}, eventType={}", message.getEventId(), message.getEventType());

        // Process the notification event here
        try {
            // identify the event type and process accordingly
            if (message.getEventType().equals("NOTIFY_ACCOUNT_CREATED_SUCCESS") && message.getEventData() instanceof AccountCreatedEvent eventData) {
                // trigger notifications
                log.info("NOTIFICATION-SERVICE-MOCK: Sending notification for account creation: accountId={}, balance={}",
                        eventData.getAccountId(), eventData.getBalance());
            }

            log.info("NOTIFICATION-SERVICE-MOCK: Notification sent successfully: data={}", message.getEventData());
        } catch (Exception e) {
            log.error("NOTIFICATION-SERVICE-MOCK: Error processing audit event {}", message.getEventId(), e);
            // Optionally send to DLQ
        }
    }
}
