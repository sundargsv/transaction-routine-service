package com.poc.transaction.utils;

import com.poc.transaction.model.events.GenericEventMessage;

import java.time.LocalDateTime;
import java.util.Map;

public class CommonUtils {
    public static <T> GenericEventMessage<T> prepareEvent(T eventData, String eventType) {
        return GenericEventMessage.<T>builder()
                .eventId(System.currentTimeMillis())
                .eventType(eventType)
                .eventDate(LocalDateTime.now().toString())
                .eventData(eventData)
                .metadata(Map.of(
                        "source", "AccountService",
                        "timestamp", LocalDateTime.now()
                ))
                .build();
    }
}
