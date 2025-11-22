package com.my.configuration;

import com.my.Main;

import java.io.IOException;
import java.util.Properties;

public class AppConfiguration {
    private static final String CONFIG_FILE_PATH = "/application.properties";
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
        properties.load(Main.class.getResourceAsStream(CONFIG_FILE_PATH));
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
}
