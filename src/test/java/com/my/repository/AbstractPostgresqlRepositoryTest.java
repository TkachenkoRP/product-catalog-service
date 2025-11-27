package com.my.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.my.configuration.ApplicationConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@WebAppConfiguration
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ApplicationConfig.class)
@Transactional
@Rollback
public abstract class AbstractPostgresqlRepositoryTest {

    @Container
    protected static final PostgreSQLContainer<?> postgresContainer =
            new PostgreSQLContainer<>("postgres:17.4")
                    .withInitScript("init.sql")
                    .waitingFor(Wait.forListeningPort());

    public static ObjectMapper objectMapper;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("datasource.url", postgresContainer::getJdbcUrl);
        registry.add("datasource.username", postgresContainer::getUsername);
        registry.add("datasource.password", postgresContainer::getPassword);
        registry.add("liquibase.contexts", () -> "test");
    }

    @BeforeAll
    static void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    @AfterAll
    static void tearDown() {
        postgresContainer.stop();
    }
}
