package com.my.service;

import java.util.List;

public interface CacheService {
    void put(String key, Object value);

    <T> T get(String key, Class<T> clazz);

    <T> List<T> getList(String key, Class<T> clazz);

    void invalidate(String key);
}
