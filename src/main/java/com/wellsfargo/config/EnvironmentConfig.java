package com.wellsfargo.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Environment-aware configuration loader.
 * Loads properties in order: application.properties → environment-specific → system overrides.
 * 
 * Usage: mvn clean test -Denv=SIT
 *        EnvironmentConfig.get("env.baseUrl") → returns SIT base URL
 */
public class EnvironmentConfig {

    private static final Properties properties = new Properties();
    private static boolean initialized = false;

    static {
        initialize();
    }

    private static synchronized void initialize() {
        if (initialized) return;

        try {
            // 1. Load global application properties
            loadFile("config/application.properties");

            // 2. Load test run properties
            loadFile("config/testrun.properties");

            // 3. Load environment-specific properties (SIT/UAT)
            String env = System.getProperty("env",
                    properties.getProperty("env.default", "SIT"));
            String envFile = "config/environments/" + env.toLowerCase() + ".properties";
            loadFile(envFile);

            System.out.println("╔══════════════════════════════════════════╗");
            System.out.println("║  Environment: " + env.toUpperCase() + "                          ║");
            System.out.println("║  Base URL: " + properties.getProperty("env.baseUrl", "N/A"));
            System.out.println("╚══════════════════════════════════════════╝");

            initialized = true;
        } catch (Exception e) {
            System.err.println("⚠ Config initialization error: " + e.getMessage());
        }
    }

    private static void loadFile(String path) {
        try (FileInputStream fis = new FileInputStream(path)) {
            properties.load(fis);
        } catch (IOException e) {
            System.out.println("⚠ Config file not found (skipped): " + path);
        }
    }

    /**
     * Get a config value. System properties override file values.
     */
    public static String get(String key) {
        return System.getProperty(key, properties.getProperty(key));
    }

    /**
     * Get a config value with a default fallback.
     */
    public static String get(String key, String defaultValue) {
        String value = get(key);
        return (value != null) ? value : defaultValue;
    }

    /**
     * Get an integer config value.
     */
    public static int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(get(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Get the current environment name (SIT/UAT).
     */
    public static String getEnvironment() {
        return get("env", get("env.default", "SIT")).toUpperCase();
    }

    /**
     * Get the base URL for the current environment.
     */
    public static String getBaseUrl() {
        return get("env.baseUrl");
    }
}
