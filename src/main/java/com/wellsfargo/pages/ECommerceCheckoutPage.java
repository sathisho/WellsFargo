package com.wellsfargo.pages;

import com.wellsfargo.config.LocatorRepository;
import com.wellsfargo.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import utils.Log;

/**
 * Enterprise Page Object for E-Commerce Checkout (TutorialsNinja).
 * 
 * Follows enterprise standards:
 *   - Locators externalized in resources/web/locators/CheckoutPage.properties
 *   - WaitUtils for all waits — NO Thread.sleep()
 *   - LocatorRepository for dynamic locator resolution
 */
public class ECommerceCheckoutPage {

    private static final String PAGE = "CheckoutPage";
    private final WebDriver driver;

    public ECommerceCheckoutPage(WebDriver driver) {
        this.driver = driver;
    }

    // ======================== Locator Helpers ========================

    private By loc(String key) {
        String raw = LocatorRepository.getLocator(PAGE, key);
        // Determine strategy from the locator properties file
        String fullEntry = getFullEntry(key);
        if (fullEntry.contains("\"locateBy\":\"css\"")) {
            return By.cssSelector(raw);
        }
        return By.xpath(raw);
    }

    private String getFullEntry(String key) {
        try {
            java.io.FileInputStream fis = new java.io.FileInputStream(
                    "resources/web/locators/" + PAGE + ".properties");
            java.util.Properties props = new java.util.Properties();
            props.load(fis);
            fis.close();
            return props.getProperty(key, "");
        } catch (Exception e) {
            return "";
        }
    }

    // ======================== Search Actions ========================

    public void enterSearchText(String text) {
        Log.info("Entering search text: " + text);
        WebElement field = WaitUtils.waitForVisible(driver, loc("search.box"));
        field.clear();
        field.sendKeys(text);
    }

    public void clickSearchIcon() {
        Log.info("Clicking search icon");
        WaitUtils.waitForClickable(driver, loc("search.icon")).click();
    }

    // ======================== Product Actions ========================

    public void clickAddToCart() {
        Log.info("Clicking Add to Cart button");
        WaitUtils.waitForClickable(driver, loc("add.to.cart.button")).click();
    }

    // ======================== Success Message ========================

    public boolean isSuccessMessageDisplayed() {
        return WaitUtils.waitForVisible(driver, loc("success.message")).isDisplayed();
    }

    public String getSuccessMessage() {
        WebElement alert = WaitUtils.waitForVisible(driver, loc("success.message"));
        String msg = alert.getText();
        Log.info("Success message: " + msg);
        return msg;
    }

    // ======================== Cart Actions ========================

    public void clickCartItemsList() {
        Log.info("Clicking cart items list");
        WaitUtils.waitForClickable(driver, loc("cart.items.list")).click();
    }

    public void clickCheckoutButton() {
        Log.info("Clicking Checkout button");
        WaitUtils.waitForClickable(driver, loc("checkout.button")).click();
    }

    // ======================== Checkout Warning ========================

    public boolean isCheckoutWarningDisplayed() {
        return WaitUtils.waitForVisible(driver, loc("checkout.warning.message")).isDisplayed();
    }

    public String getCheckoutWarningMessage() {
        WebElement alert = WaitUtils.waitForVisible(driver, loc("checkout.warning.message"));
        String msg = alert.getText();
        Log.info("Checkout warning: " + msg);
        return msg;
    }
}
