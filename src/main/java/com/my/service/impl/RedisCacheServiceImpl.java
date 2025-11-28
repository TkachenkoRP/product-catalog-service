package com.my.service.impl;

import com.my.service.CacheService;
import com.my.service.RedisCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RedisCacheServiceImpl implements CacheService {

    private final RedisCacheService redisCacheService;

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
