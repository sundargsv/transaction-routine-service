package com.poc.transaction.service.cache;

public interface CacheService {
    /**
     * Saves an object in the cache with the specified key.
     *
     * @param key   the key under which the object is stored
     * @param value the object to be cached
     */
    void set(String key, Object value);

    /**
     * Retrieves an object from the cache by its key.
     *
     * @param key the key of the cached object
     * @return the cached object, or null if not found
     */
    Object get(String key);

    /**
     * Removes an object from the cache by its key to invalidate it.
     *
     * @param key the key of the cached object to be removed
     */
    void remove(String key);
}
