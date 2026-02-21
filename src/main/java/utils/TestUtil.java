package utils;

import org.openqa.selenium.WebDriver;

public class TestUtil {
    public static void sleep(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    // Add more reusable utilities as needed
}
