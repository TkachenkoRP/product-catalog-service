package com.my.configuration;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class LiquibaseConfig {
    private final DataSource dataSource;

    @Value("${liquibase.change-log}")
    private String changeLog;

    @Value("${liquibase.contexts}")
    private String contexts;

    @Value("${liquibase.schema}")
    private String liquibaseSchema;

    @Autowired
    public LiquibaseConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Bean
    public SpringLiquibase liquibase() {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog(changeLog);
        liquibase.setLiquibaseSchema(liquibaseSchema);
        liquibase.setContexts(contexts);
        return liquibase;
    }
}
