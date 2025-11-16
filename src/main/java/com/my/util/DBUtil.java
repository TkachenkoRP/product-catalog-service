package com.my.util;

import com.my.configuration.AppConfiguration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Утилитарный класс для работы с базой данных.
 *
 * <p>Предоставляет методы для установления соединения с PostgreSQL базой данных.
 * Использует настройки из {@link AppConfiguration} для формирования connection string.
 */
public class DBUtil {
    private DBUtil() {
    }

    /**
     * Создает соединение с базой данных используя настройки по умолчанию.
     *
     * @return соединение с базой данных
     * @throws SQLException если возникает ошибка при установлении соединения
     */
    public static Connection getConnection() throws SQLException {
        return getConnection(AppConfiguration.getProperty("database.schema"));
    }

    /**
     * Создает соединение с базой данных с указанной схемой.
     *
     * @param schema схема базы данных
     * @return соединение с базой данных с установленной схемой
     * @throws SQLException если возникает ошибка при установлении соединения
     */
    public static Connection getConnection(String schema) throws SQLException {
        String url = AppConfiguration.getProperty("database.url");
        String username = AppConfiguration.getProperty("database.username");
        String password = AppConfiguration.getProperty("database.password");
        return getConnection(url, username, password, schema);
    }

    /**
     * Создает соединение с базой данных с указанными параметрами.
     *
     * @param url      URL базы данных
     * @param username имя пользователя
     * @param password пароль
     * @param schema   схема базы данных
     * @return соединение с базой данных
     * @throws SQLException если возникает ошибка при установлении соединения
     */
    public static Connection getConnection(String url, String username, String password, String schema) throws SQLException {
        Connection connection = DriverManager.getConnection(url, username, password);
        connection.setSchema(schema);
        return connection;
    }
}
