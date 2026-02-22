package com.wellsfargo.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utility to load external test data files (ISO, SWIFT, Pega messages).
 * Reads XML/JSON files from resources-testdata/ directory.
 *
 * Usage:
 *   String isoMsg = TestDataLoader.loadMessage("ISO", "pacs.008.xml");
 */
public class TestDataLoader {

    private static final String TESTDATA_BASE = "resources-testdata";

    /**
     * Load a test data file as String.
     *
     * @param category e.g., "ISO", "SWIFT", "Pega"
     * @param fileName e.g., "pacs.008.xml"
     * @return file content as String
     */
    public static String loadMessage(String category, String fileName) {
        Path filePath = Path.of(TESTDATA_BASE, category, fileName);
        File file = filePath.toFile();

        if (!file.exists()) {
            throw new RuntimeException("Test data file not found: " + filePath);
        }

        try {
            String content = Files.readString(filePath);
            System.out.println("✔ Loaded test data: " + filePath + " (" + content.length() + " chars)");
            return content;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read test data: " + filePath, e);
        }
    }

    /**
     * Check if a test data file exists.
     */
    public static boolean exists(String category, String fileName) {
        return Path.of(TESTDATA_BASE, category, fileName).toFile().exists();
    }

    /**
     * Load and return file as byte array (for binary data).
     */
    public static byte[] loadBytes(String category, String fileName) {
        Path filePath = Path.of(TESTDATA_BASE, category, fileName);
        try {
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read binary test data: " + filePath, e);
        }
    }
}
