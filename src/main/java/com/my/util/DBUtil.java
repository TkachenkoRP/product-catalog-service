package com.my.util;

import com.my.configuration.AppConfiguration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {
    private DBUtil() {
    }

    public static Connection getConnection() throws SQLException {
        return getConnection(AppConfiguration.getProperty("database.schema"));
    }

    public static Connection getConnection(String schema) throws SQLException {
        String url = AppConfiguration.getProperty("database.url");
        String username = AppConfiguration.getProperty("database.username");
        String password = AppConfiguration.getProperty("database.password");
        return getConnection(url, username, password, schema);
    }

    public static Connection getConnection(String url, String username, String password, String schema) throws SQLException {
        Connection connection = DriverManager.getConnection(url, username, password);
        connection.setSchema(schema);
        return connection;
    }
}
