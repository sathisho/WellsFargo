package com.wellsfargo.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Loads externalized locators from .properties files.
 * Locators are stored in JSON-style format:
 *   key={"locateBy":"xpath","locator":"//div[@id='test']"}
 *
 * Usage:
 *   LocatorRepository.getLocator("LoginPage", "username.field")
 *   → returns "//input[@id='username']"
 */
public class LocatorRepository {

    private static final Map<String, Properties> locatorCache = new HashMap<>();
    private static final String LOCATOR_BASE_PATH = "resources/web/locators/";

    /**
     * Get a locator value by page name and locator key.
     *
     * @param pageName   e.g., "LoginPage"
     * @param locatorKey e.g., "username.field"
     * @return the XPath or CSS locator string
     */
    public static String getLocator(String pageName, String locatorKey) {
        Properties pageLocators = locatorCache.computeIfAbsent(pageName, LocatorRepository::loadLocators);
        String raw = pageLocators.getProperty(locatorKey);

        if (raw == null) {
            throw new RuntimeException("Locator not found: " + pageName + "." + locatorKey);
        }

        // Parse JSON-style: {"locateBy":"xpath","locator":"//div[@id='test']"}
        if (raw.contains("\"locator\"")) {
            int start = raw.indexOf("\"locator\"") + 11;
            int end = raw.lastIndexOf("\"");
            return raw.substring(start, end);
        }

        return raw;
    }

    private static Properties loadLocators(String pageName) {
        Properties props = new Properties();
        String path = LOCATOR_BASE_PATH + pageName + ".properties";
        try (FileInputStream fis = new FileInputStream(path)) {
            props.load(fis);
        } catch (IOException e) {
            System.err.println("⚠ Locator file not found: " + path);
        }
        return props;
    }
}
