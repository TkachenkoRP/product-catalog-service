package com.my.util;

import com.my.configuration.AppConfiguration;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

import java.sql.Connection;

public class LiquibaseManager {

    private LiquibaseManager() {
    }

    private static final String CHANGELOG_FILE_PROPERTY = "liquibase.change-log";

    /**
     * Выполняет обновление базы данных с использованием Liquibase
     *
     * @param connection соединение с базой данных
     * @param context контекст выполнения
     */
    public static void updateDatabase(Connection connection, String context) {
        String changelogFile = AppConfiguration.getProperty(CHANGELOG_FILE_PROPERTY);
        Contexts contexts = new Contexts(context);

        try {
            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));
            Liquibase liquibase = new Liquibase(changelogFile, new ClassLoaderResourceAccessor(), database);
            liquibase.update(contexts);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка выполнения миграций Liquibase", e);
        }
    }

    /**
     * Выполняет обновление базы данных с контекстом по умолчанию
     *
     * @param connection соединение с базой данных
     */
    public static void updateDatabase(Connection connection) {
        updateDatabase(connection, AppConfiguration.getProperty("liquibase.contexts"));
    }
}
