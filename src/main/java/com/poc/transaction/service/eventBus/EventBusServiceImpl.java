package com.poc.transaction.service.eventBus;

import com.poc.transaction.model.events.GenericEventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventBusServiceImpl implements EventBusService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    // TODO: Implement a error handling and retry mechanism for Kafka publishing - like spring kafka callBack's
    @Override
    public void publishEvent(String topic, String key, GenericEventMessage data) {
        try {
            kafkaTemplate.send(topic, key, data);
            log.info("{} Event published to topic [{}] with key: {}", data.getEventType(), topic, key);
        } catch (Exception e) {
            log.error("Failed to publish event to topic [{}] with key: {}", topic, e, key);
        }
    }

    @Override
    public void publishEvent(String topic, GenericEventMessage data) {
        try {
            kafkaTemplate.send(topic, data);
            log.info("{} Event published to topic [{}]", data.getEventType(), topic);
        } catch (Exception e) {
            log.error("Failed to publish event to topic [{}]", topic, e);
        }
    }

}
