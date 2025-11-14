package com.my.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CacheService {
    private final Map<String, Object> cache = new ConcurrentHashMap<>();
    private static final long CACHE_DURATION = 3 * 60 * 1000L;
    private final Map<String, Long> cacheTimestamps = new ConcurrentHashMap<>();

    public void put(String key, Object value) {
        cache.put(key, value);
        cacheTimestamps.put(key, System.currentTimeMillis());
    }

    public Object get(String key) {
        Long timestamp = cacheTimestamps.get(key);
        if (timestamp != null && System.currentTimeMillis() - timestamp > CACHE_DURATION) {
            cache.remove(key);
            cacheTimestamps.remove(key);
            return null;
        }
        return cache.get(key);
    }

    public void invalidate(String key) {
        cache.remove(key);
        cacheTimestamps.remove(key);
    }

    public enum CacheKey {
        ALL_PRODUCTS,
        PRODUCT,
        ALL_CATEGORIES,
        CATEGORY,
        ALL_BRANDS,
        BRAND
    }
}
