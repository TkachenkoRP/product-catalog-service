package com.my.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class SequenceGenerator {
    private final JdbcTemplate jdbcTemplate;

    @Value("${datasource.schema}")
    private String schema;

    @Autowired
    public SequenceGenerator(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public Long getNextSequenceValue(String sequenceName) {
        String sql = String.format("SELECT nextval('%s.%s')", schema, sequenceName);
        return jdbcTemplate.queryForObject(sql, Long.class);
    }
}
