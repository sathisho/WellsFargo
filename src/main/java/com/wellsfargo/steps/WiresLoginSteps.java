package com.wellsfargo.steps;

import com.wellsfargo.pages.WiresLoginPage;
import com.wellsfargo.pages.WiresPaymentPage;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.WebDriver;
import utils.DriverFactory;
import utils.Log;

/**
 * QAF-style Step Definitions for WIRES Login.
 * Reusable across all scenarios that require WIRES authentication.
 */
public class WiresLoginSteps {

    private final WebDriver driver = DriverFactory.getDriver();
    private final WiresLoginPage loginPage = new WiresLoginPage(driver);

    @Given("User logs into WIRES application")
    public void user_logs_into_wires_application() {
        Log.info("Step: User logs into WIRES application");
        loginPage.openLoginPage();
        loginPage.loginWithDefaultCredentials();
    }

    @Given("User logs into WIRES with username {string} and password {string}")
    public void user_logs_into_wires_with_credentials(String username, String password) {
        Log.info("Step: User logs into WIRES with username: " + username);
        loginPage.openLoginPage();
        loginPage.login(username, password);
    }

    @Then("User should be logged in successfully")
    public void user_should_be_logged_in_successfully() {
        if (!loginPage.isLoginSuccessful()) {
            throw new AssertionError("WIRES login was not successful — welcome message not displayed");
        }
        Log.info("Step: Login verified successfully");
    }
}
