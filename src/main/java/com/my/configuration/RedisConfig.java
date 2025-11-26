package com.my.configuration;

import lombok.Getter;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisConfig {
    private RedisConfig() {
    }

    @Getter
    private static final JedisPool jedisPool;

    static {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(128);

        String host = AppConfiguration.getProperty("redis.host");
        int port = Integer.parseInt(AppConfiguration.getProperty("redis.port"));
        int timeout = Integer.parseInt(AppConfiguration.getProperty("redis.timeout"));
        String password = AppConfiguration.getProperty("redis.password");
        int database = Integer.parseInt(AppConfiguration.getProperty("redis.database"));

        jedisPool = new JedisPool(poolConfig, host, port, timeout, password, database);
    }

}
