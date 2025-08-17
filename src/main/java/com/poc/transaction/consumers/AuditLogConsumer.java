package com.poc.transaction.consumers;

import com.poc.transaction.model.events.GenericEventMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.poc.transaction.utils.Constants.KafkaTopics.AUDIT_LOG;

@Slf4j
@Service
public class AuditLogConsumer {

    @KafkaListener(
            topics = AUDIT_LOG, // your audit log topic
            groupId = "consumer", // no consumer group id since its running in only one instance
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(GenericEventMessage<Map<String, Object>> message) {
        log.info("AUDIT-LOG-SERVICE-MOCK: Received audit event: eventId={}, eventType={}", message.getEventId(), message.getEventType());

        Map<String, Object> data = message.getEventData();
        log.info("AUDIT-LOG-SERVICE-MOCK: Event data: {}", data);

        // Process the audit event here
        try {
            // e.g., save audit info to DB, trigger notifications, etc.
            log.info("AUDIT-LOG-SERVICE-MOCK: Audit logged to elastic search for reporting: data={}", message.getEventData());
        } catch (Exception e) {
            log.error("AUDIT-LOG-SERVICE-MOCK: Error processing audit event {}", message.getEventId(), e);
            // Optionally send to DLQ
        }
    }
}
