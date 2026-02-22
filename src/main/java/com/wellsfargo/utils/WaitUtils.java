package com.wellsfargo.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Enterprise wait utility — NO Thread.sleep().
 * Uses explicit WebDriverWait with configurable timeouts.
 */
public class WaitUtils {

    private static final int DEFAULT_TIMEOUT = 20;
    private static final int SHORT_TIMEOUT = 5;
    private static final int LONG_TIMEOUT = 45;

    /**
     * Wait for element to be visible.
     */
    public static WebElement waitForVisible(WebDriver driver, By locator) {
        return waitForVisible(driver, locator, DEFAULT_TIMEOUT);
    }

    public static WebElement waitForVisible(WebDriver driver, By locator, int timeoutSeconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
                .until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /**
     * Wait for element to be clickable, then return it.
     */
    public static WebElement waitForClickable(WebDriver driver, By locator) {
        return waitForClickable(driver, locator, DEFAULT_TIMEOUT);
    }

    public static WebElement waitForClickable(WebDriver driver, By locator, int timeoutSeconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
                .until(ExpectedConditions.elementToBeClickable(locator));
    }

    /**
     * Wait for element to be present in DOM (may not be visible).
     */
    public static WebElement waitForPresence(WebDriver driver, By locator) {
        return new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT))
                .until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    /**
     * Wait for element to disappear from the page.
     */
    public static boolean waitForInvisible(WebDriver driver, By locator) {
        return new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT))
                .until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    /**
     * Wait for page title to contain text.
     */
    public static boolean waitForTitleContains(WebDriver driver, String titlePart) {
        return new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT))
                .until(ExpectedConditions.titleContains(titlePart));
    }

    /**
     * Wait for a specific text to appear in an element.
     */
    public static boolean waitForTextInElement(WebDriver driver, By locator, String text) {
        return new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT))
                .until(ExpectedConditions.textToBePresentInElementLocated(locator, text));
    }

    /**
     * Short wait (5s) for fast-loading elements.
     */
    public static WebElement waitShort(WebDriver driver, By locator) {
        return waitForVisible(driver, locator, SHORT_TIMEOUT);
    }

    /**
     * Long wait (45s) for slow operations (reports, file processing).
     */
    public static WebElement waitLong(WebDriver driver, By locator) {
        return waitForVisible(driver, locator, LONG_TIMEOUT);
    }
}
