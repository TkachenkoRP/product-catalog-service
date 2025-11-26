package com.my.util;

public class ConnectionProviderFactory {

    private ConnectionProviderFactory() {
    }

    private static ConnectionProvider defaultProvider;

    /**
     * Получить провайдер соединений по умолчанию
     */
    public static ConnectionProvider getDefaultProvider() {
        if (defaultProvider == null) {
            defaultProvider = new DefaultConnectionProvider();
        }
        return defaultProvider;
    }
}
