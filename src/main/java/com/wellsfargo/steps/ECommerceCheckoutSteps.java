package com.wellsfargo.steps;

import com.wellsfargo.config.EnvironmentConfig;
import com.wellsfargo.pages.ECommerceCheckoutPage;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.WebDriver;
import utils.DriverFactory;
import utils.Log;

/**
 * Enterprise Step Definitions for E-Commerce Checkout (TutorialsNinja).
 *
 * Maps to: scenarios/Smoke/Checkout.feature
 * Replaces: stepdefinitions.CheckoutSteps + stepdefinitions.LoginSteps (Home page step)
 *
 * Enterprise standards applied:
 *   - NO Thread.sleep() — uses WaitUtils via Page Object
 *   - Externalized locators via LocatorRepository
 *   - Config-driven base URL via EnvironmentConfig / ConfigReader
 *   - TestResultCollector integration for dashboard reporting
 */
public class ECommerceCheckoutSteps {

    private final WebDriver driver = DriverFactory.getDriver();
    private final ECommerceCheckoutPage checkoutPage = new ECommerceCheckoutPage(driver);

    // ======================== Navigation ========================

    @Given("User is on Home page")
    public void user_is_on_home_page() {
        String baseUrl = EnvironmentConfig.get("baseUrl");
        if (baseUrl == null || baseUrl.isEmpty()) {
            baseUrl = config.ConfigReader.getProperty("baseUrl");
        }
        Log.info("Navigating to Home page: " + baseUrl);
        driver.get(baseUrl);
    }

    // ======================== Search ========================

    @When("User enters {string} in the search box")
    public void user_enters_in_the_search_box(String productName) {
        Log.info("Step: User enters '" + productName + "' in the search box");
        checkoutPage.enterSearchText(productName);
    }

    @And("User clicks on the search icon")
    public void user_clicks_on_the_search_icon() {
        Log.info("Step: User clicks on the search icon");
        checkoutPage.clickSearchIcon();
    }

    // ======================== Add to Cart ========================

    @And("User clicks on Add to Cart button")
    public void user_clicks_on_add_to_cart_button() {
        Log.info("Step: User clicks on Add to Cart button");
        checkoutPage.clickAddToCart();
    }

    @Then("User should see the success message {string}")
    public void user_should_see_the_success_message(String expectedMessage) {
        Log.info("Step: Verifying success message contains: " + expectedMessage);
        if (!checkoutPage.isSuccessMessageDisplayed()) {
            throw new AssertionError("Success message is not displayed");
        }
        String actualMessage = checkoutPage.getSuccessMessage();
        if (!actualMessage.contains(expectedMessage)) {
            throw new AssertionError("Expected success message to contain: '"
                    + expectedMessage + "' but was: '" + actualMessage + "'");
        }
        Log.info("✅ Success message verified");
    }

    // ======================== Cart & Checkout ========================

    @When("User clicks on the cart items list")
    public void user_clicks_on_the_cart_items_list() {
        Log.info("Step: User clicks on the cart items list");
        checkoutPage.clickCartItemsList();
    }

    @And("User clicks on the Checkout button")
    public void user_clicks_on_the_checkout_button() {
        Log.info("Step: User clicks on the Checkout button");
        checkoutPage.clickCheckoutButton();
    }

    @Then("User should see the checkout warning message {string}")
    public void user_should_see_the_checkout_warning_message(String expectedMessage) {
        Log.info("Step: Verifying checkout warning contains: " + expectedMessage);
        if (!checkoutPage.isCheckoutWarningDisplayed()) {
            throw new AssertionError("Checkout warning message is not displayed");
        }
        String actualMessage = checkoutPage.getCheckoutWarningMessage();
        if (!actualMessage.contains(expectedMessage)) {
            throw new AssertionError("Expected checkout warning to contain: '"
                    + expectedMessage + "' but was: '" + actualMessage + "'");
        }
        Log.info("✅ Checkout warning verified");
    }
}
