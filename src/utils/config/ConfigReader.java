package com.framework.utils.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {
    private static Properties properties;

    // Load config file only once (Singleton Pattern)
    static {
        try {
            properties = new Properties();
            String configFilePath = "src/test/resources/config.properties";
            FileInputStream fis = new FileInputStream(configFilePath);
            properties.load(fis);
            fis.close();
        } catch (IOException e) {
            throw new RuntimeException("❌ Failed to load config.properties file", e);
        }
    }

    // Retrieve property values
    public static String getProperty(String key) {
        return properties.getProperty(key, "").trim();  // Returns empty string if key is missing
    }

    public static int getIntProperty(String key, int defaultValue) {
        try {
            return Integer.parseInt(getProperty(key));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = getProperty(key);
        return value.isEmpty() ? defaultValue : Boolean.parseBoolean(value);
    }
}
