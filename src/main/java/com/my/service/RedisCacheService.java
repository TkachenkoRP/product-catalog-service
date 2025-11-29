package com.my.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.configuration.JacksonConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisCacheService {
    private final JedisPool jedisPool;
    private final ObjectMapper objectMapper  = JacksonConfig.createObjectMapper();;

    @Value("${redis.cache.ttl.minutes}")
    private int defaultTtlMinutes;

    public void put(String key, Object value) {
        put(key, value, defaultTtlMinutes);
    }

    public void put(String key, Object value, int ttlMinutes) {
        try (Jedis jedis = jedisPool.getResource()) {
            String jsonValue = objectMapper.writeValueAsString(value);
            jedis.setex(key, TimeUnit.MINUTES.toSeconds(ttlMinutes), jsonValue);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Ошибка сериализации объекта для кеша", e);
        }
    }

    public <T> T get(String key, Class<T> clazz) {
        try (Jedis jedis = jedisPool.getResource()) {
            String jsonValue = jedis.get(key);
            if (jsonValue == null) {
                return null;
            }
            return objectMapper.readValue(jsonValue, clazz);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка десериализации объекта из кеша", e);
        }
    }

    public <T> List<T> getList(String key, Class<T> clazz) {
        try (Jedis jedis = jedisPool.getResource()) {
            String jsonValue = jedis.get(key);
            if (jsonValue == null) {
                return null;
            }
            return objectMapper.readValue(jsonValue,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (IOException e) {
            throw new RuntimeException("Ошибка десериализации списка из кеша", e);
        }
    }

    public void invalidate(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(key);
        }
    }
}
