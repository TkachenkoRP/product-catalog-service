package com.my.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Утилитарный класс для настройки ObjectMapper Jackson.
 */
public class JacksonConfig {

    private JacksonConfig() {
    }

    /**
     * Создает и настраивает экземпляр ObjectMapper.
     *
     * @return настроенный ObjectMapper
     */
    public static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return mapper;
    }
}
