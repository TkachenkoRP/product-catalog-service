package com.my.util;

import com.my.configuration.AppConfiguration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Реализация провайдера соединений с базой данных
 */
public class DefaultConnectionProvider implements ConnectionProvider {

    private static final String DATABASE_URL = "database.url";
    private static final String DATABASE_USERNAME = "database.username";
    private static final String DATABASE_PASSWORD = "database.password";
    private static final String DATABASE_SCHEMA = "database.schema";

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL Driver не найден", e);
        }
    }

    @Override
    public Connection getConnection() {
        return getConnection(AppConfiguration.getProperty(DATABASE_SCHEMA));
    }

    @Override
    public Connection getConnection(String schema) {
        String url = AppConfiguration.getProperty(DATABASE_URL);
        String username = AppConfiguration.getProperty(DATABASE_USERNAME);
        String password = AppConfiguration.getProperty(DATABASE_PASSWORD);
        return getConnection(url, username, password, schema);
    }

    @Override
    public Connection getConnection(String url, String username, String password, String schema) {
        try {
            Connection connection = DriverManager.getConnection(url, username, password);
            connection.setSchema(schema);
            return connection;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка создания соединения с базой данных", e);
        }
    }
}
