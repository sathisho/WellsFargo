package com.wellsfargo.pages;

import com.wellsfargo.config.EnvironmentConfig;
import com.wellsfargo.config.LocatorRepository;
import com.wellsfargo.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import utils.Log;

/**
 * Page Object for WIRES Login Page.
 * All locators externalized in resources/web/locators/LoginPage.properties.
 */
public class WiresLoginPage {

    private final WebDriver driver;

    // Locators loaded from LoginPage.properties
    private By usernameField() {
        return By.xpath(LocatorRepository.getLocator("LoginPage", "username.field"));
    }

    private By passwordField() {
        return By.xpath(LocatorRepository.getLocator("LoginPage", "password.field"));
    }

    private By loginButton() {
        return By.xpath(LocatorRepository.getLocator("LoginPage", "login.button"));
    }

    private By welcomeMessage() {
        return By.xpath(LocatorRepository.getLocator("LoginPage", "welcome.message"));
    }

    public WiresLoginPage(WebDriver driver) {
        this.driver = driver;
    }

    /**
     * Navigate to WIRES login page using environment-specific URL.
     */
    public void openLoginPage() {
        String baseUrl = EnvironmentConfig.getBaseUrl();
        Log.info("Opening WIRES login page: " + baseUrl);
        driver.get(baseUrl);
    }

    /**
     * Login with environment-configured credentials.
     */
    public void loginWithDefaultCredentials() {
        String username = EnvironmentConfig.get("env.username");
        String password = EnvironmentConfig.get("env.password");
        login(username, password);
    }

    /**
     * Login with explicit credentials.
     */
    public void login(String username, String password) {
        Log.info("Logging in as: " + username);

        WebElement userField = WaitUtils.waitForVisible(driver, usernameField());
        userField.clear();
        userField.sendKeys(username);

        WebElement passField = WaitUtils.waitForVisible(driver, passwordField());
        passField.clear();
        passField.sendKeys(password);

        WaitUtils.waitForClickable(driver, loginButton()).click();
        Log.info("Login submitted");
    }

    /**
     * Verify login was successful by checking welcome message.
     */
    public boolean isLoginSuccessful() {
        try {
            WebElement welcome = WaitUtils.waitForVisible(driver, welcomeMessage());
            Log.info("Login successful — Welcome: " + welcome.getText());
            return welcome.isDisplayed();
        } catch (Exception e) {
            Log.error("Login verification failed: " + e.getMessage());
            return false;
        }
    }
}
