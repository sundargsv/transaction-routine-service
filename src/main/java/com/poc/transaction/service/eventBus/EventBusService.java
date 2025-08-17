package com.poc.transaction.service.eventBus;

import com.poc.transaction.model.events.GenericEventMessage;

public interface EventBusService {

    /**
     * Publish event with a key (ordering guaranteed per key/partition)
     *
     * @param topic Kafka topic name
     * @param key   Key for partitioning (e.g., accountId)
     * @param data  Type of the event data object to send
     */
    void publishEvent(String topic, String key, GenericEventMessage data);

    /**
     * Publish event without a key
     *
     * @param topic Kafka topic name
     * @param data  Type of the event data object to send
     */
    void publishEvent(String topic, GenericEventMessage data);
}
