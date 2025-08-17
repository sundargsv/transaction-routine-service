package com.poc.transaction.model.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
/*
* Use a Generic Events for audit logging, tracking, analytics where flexibility > schema safety.
* */
public class GenericEventMessage<T> {
    private long eventId;
    private String eventType;
    private String eventDate;
    private T eventData; // JSON string representation of the event data
    private Map<String, Object> metadata; // Additional metadata for the event
}
