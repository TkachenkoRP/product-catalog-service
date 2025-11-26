package com.my.util;

import java.sql.Connection;

/**
 * Интерфейс для предоставления соединений с базой данных
 */
public interface ConnectionProvider {
    /**
     * Получить соединение с базой данных с настройками по умолчанию
     */
    Connection getConnection();

    /**
     * Получить соединение с базой данных с указанной схемой
     */
    Connection getConnection(String schema);

    /**
     * Получить соединение с базой данных с указанными параметрами
     */
    Connection getConnection(String url, String username, String password, String schema);
}
