package com.my.repository;

import com.my.configuration.AppConfiguration;
import com.my.repository.impl.PostgresqlBrandRepositoryImpl;
import com.my.repository.impl.PostgresqlCategoryRepositoryImpl;
import com.my.repository.impl.PostgresqlUserRepositoryImpl;
import com.my.util.DBUtil;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Testcontainers
public class AbstractPostgresqlRepositoryTest {

    @Container
    protected static final PostgreSQLContainer<?> postgresContainer =
            new PostgreSQLContainer<>("postgres:17.4")
                    .waitingFor(Wait.forListeningPort());

    protected static Connection testConnection;
    protected static String testSchema = AppConfiguration.getProperty("database.schema");

    protected static UserRepository userRepository;
    protected static CategoryRepository categoryRepository;
    protected static BrandRepository brandRepository;

    @BeforeAll
    static void setUp() throws SQLException, LiquibaseException {
        String jdbcUrl = postgresContainer.getJdbcUrl();
        String username = postgresContainer.getUsername();
        String password = postgresContainer.getPassword();

        testConnection = DBUtil.getConnection(jdbcUrl, username, password, testSchema);

        try (Statement statement = testConnection.createStatement()) {
            statement.executeUpdate("CREATE SCHEMA IF NOT EXISTS %s;".formatted(testSchema));
        }

        Contexts contexts = new Contexts("test");
        String changelogFile = AppConfiguration.getProperty("liquibase.change-log");
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(testConnection));
        Liquibase liquibase = new Liquibase(changelogFile, new ClassLoaderResourceAccessor(), database);
        liquibase.update(contexts);

        userRepository = new PostgresqlUserRepositoryImpl(testConnection);
        categoryRepository = new PostgresqlCategoryRepositoryImpl(testConnection);
        brandRepository = new PostgresqlBrandRepositoryImpl(testConnection);

    }

    @BeforeEach
    void setUpTransaction() throws SQLException {
        testConnection.setAutoCommit(false);
    }

    @AfterEach
    void tearDownTransaction() throws SQLException {
        testConnection.rollback();
    }
    @AfterAll
    static void tearDown() throws SQLException {
        testConnection.close();
        postgresContainer.stop();
    }
}
