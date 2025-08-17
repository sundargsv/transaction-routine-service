package com.poc.transaction.utils;

//Ideally application wide constants should be in a single class for better readability
public final class Constants {

    public static final String X_TRACE_ID = "X-Trace-Id";

    public static final String LOGGER_PREFIX = "HTTP-LOG";

    public static final String CACHE_PREFIX = "account:";

    public static class KafkaTopics {
        public static final String NOTIFICATION = "notification.requested";
        public static final String AUDIT_LOG = "audit.log.requested";
    }

    //No instance can be created
    private Constants() {}
}
