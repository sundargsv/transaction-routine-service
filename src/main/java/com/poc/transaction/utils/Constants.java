package com.poc.transaction.utils;

//Ideally application wide constants should be in a single class for better readability
public final class Constants {

    public static final String X_TRACE_ID = "X-Trace-Id";

    public static final String LOGGER_PREFIX = "HTTP-LOG";

    public static final String CACHE_PREFIX = "account:";

    //No instance can be created
    private Constants() {}
}
