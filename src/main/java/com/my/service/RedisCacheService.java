package com.my.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.configuration.AppConfiguration;
import com.my.configuration.JacksonConfig;
import com.my.configuration.RedisConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RedisCacheService {
    private final JedisPool jedisPool;
    private final ObjectMapper objectMapper;
    private final int defaultTtlMinutes;

    public RedisCacheService() {
        this(RedisConfig.getJedisPool(), Integer.parseInt(AppConfiguration.getProperty("redis.cache.ttl.minutes")));
    }

    public RedisCacheService(JedisPool jedisPool, int defaultTtlMinutes) {
        this.jedisPool = jedisPool;
        this.objectMapper = JacksonConfig.createObjectMapper();
        this.defaultTtlMinutes = defaultTtlMinutes;
    }

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
