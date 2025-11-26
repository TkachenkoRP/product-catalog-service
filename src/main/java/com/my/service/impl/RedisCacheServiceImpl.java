package com.my.service.impl;

import com.my.service.CacheService;
import com.my.service.RedisCacheService;

import java.util.List;

public class RedisCacheServiceImpl implements CacheService {

    private final RedisCacheService redisCacheService;

    public RedisCacheServiceImpl() {
        this(new RedisCacheService());
    }

    public RedisCacheServiceImpl(RedisCacheService redisCacheService) {
        this.redisCacheService = redisCacheService;
    }

    @Override
    public void put(String key, Object value) {
        redisCacheService.put(key, value);
    }

    @Override
    public <T> T get(String key, Class<T> clazz) {
        return redisCacheService.get(key, clazz);
    }

    @Override
    public <T> List<T> getList(String key, Class<T> clazz) {
        return redisCacheService.getList(key, clazz);
    }

    @Override
    public void invalidate(String key) {
        redisCacheService.invalidate(key);
    }
}
