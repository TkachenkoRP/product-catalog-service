package com.my.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * Компонент для генерации последовательных значений из баз данных, поддерживающих последовательности.
 * Использует SQL-функцию {@code nextval()} для получения следующего значения последовательности.
 */
@Component
public class SequenceGenerator {
    private final JdbcTemplate jdbcTemplate;

    @Value("${datasource.schema}")
    private String schema;

    /**
     * Конструктор с внедрением зависимости DataSource.
     *
     * @param dataSource источник данных для подключения к базе данных
     */
    @Autowired
    public SequenceGenerator(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * Получает следующее значение из указанной последовательности.
     *
     * @param sequenceName имя последовательности в базе данных
     * @return следующее значение последовательности
     * @throws org.springframework.dao.DataAccessException если возникает ошибка при выполнении запроса
     */
    public Long getNextSequenceValue(String sequenceName) {
        String sql = String.format("SELECT nextval('%s.%s')", schema, sequenceName);
        return jdbcTemplate.queryForObject(sql, Long.class);
    }
}
