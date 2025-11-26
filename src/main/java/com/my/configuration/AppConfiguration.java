package com.my.configuration;

import com.my.Main;

import java.io.IOException;
import java.util.Properties;

public class AppConfiguration {
    private static final String CONFIG_FILE_PATH = "/application.properties";
    private static Properties properties;

    private AppConfiguration() {
    }

    private static void init() {
        if (properties == null) {
            properties = new Properties();
            try {
                loadConfig();
            } catch (IOException e) {
                throw new RuntimeException("Failed to load application configuration", e);
            }
        }
    }

    private static void loadConfig() throws IOException {
        properties.load(Main.class.getResourceAsStream(CONFIG_FILE_PATH));
    }

    public static String getProperty(String key) {
        init();
        return properties.getProperty(key);
    }
}
