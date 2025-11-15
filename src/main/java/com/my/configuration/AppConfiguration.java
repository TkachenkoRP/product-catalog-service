package com.my.configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class AppConfiguration {
    private static final String CONFIG_FILE_PATH = "src/main/resources/application.properties";
    private static final Properties properties;

    private AppConfiguration() {
    }

    static {
        properties = new Properties();
        try {
            loadConfig();
        } catch (IOException e) {
            System.err.println("Failed to load database configuration: " + e.getMessage());
        }
    }

    private static void loadConfig() throws IOException {
        try (FileInputStream inputStream = new FileInputStream(CONFIG_FILE_PATH)) {
            properties.load(inputStream);
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
}
