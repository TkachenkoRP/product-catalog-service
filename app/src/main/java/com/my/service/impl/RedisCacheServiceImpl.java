package com.my.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.configuration.JacksonConfig;
import com.my.exception.CacheException;
import com.my.service.CacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisCacheServiceImpl implements CacheService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper = JacksonConfig.createObjectMapper();

    @Value("${spring.data.redis.cache.ttl.minutes}")
    private int defaultTtlMinutes;

    @Override
    public void put(String key, Object value) {
        try {
            String jsonValue = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, jsonValue, defaultTtlMinutes, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            throw new CacheException("Ошибка сериализации объекта для кеша", e);
        }
    }

    @Override
    public <T> T get(String key, Class<T> clazz) {
        try {
            String jsonValue = redisTemplate.opsForValue().get(key);
            if (jsonValue == null) {
                return null;
            }
            return objectMapper.readValue(jsonValue, clazz);
        } catch (JsonProcessingException e) {
            throw new CacheException("Ошибка десериализации объекта из кеша", e);
        }
    }

    @Override
    public <T> List<T> getList(String key, Class<T> clazz) {
        try {
            String jsonValue = redisTemplate.opsForValue().get(key);
            if (jsonValue == null) {
                return null;
            }
            return objectMapper.readValue(jsonValue, objectMapper.getTypeFactory().constructCollectionType(List.class, clazz)
            );
        } catch (JsonProcessingException e) {
            throw new CacheException("Ошибка десериализации списка из кеша", e);
        }
    }

    @Override
    public void invalidate(String key) {
        redisTemplate.delete(key);
    }
}
