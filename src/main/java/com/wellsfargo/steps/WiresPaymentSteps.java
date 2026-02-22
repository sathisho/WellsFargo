package com.wellsfargo.steps;

import com.wellsfargo.pages.WiresPaymentPage;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.WebDriver;
import utils.DriverFactory;
import utils.Log;

/**
 * QAF-style Step Definitions for Payment operations.
 * Parameterized steps for amount and beneficiary.
 * Delegates all UI interactions to WiresPaymentPage (Page Object Model).
 */
public class WiresPaymentSteps {

    private final WebDriver driver = DriverFactory.getDriver();
    private final WiresPaymentPage paymentPage = new WiresPaymentPage(driver);

    @When("User navigates to Create Payment page")
    public void user_navigates_to_create_payment_page() {
        Log.info("Step: User navigates to Create Payment page");
        paymentPage.openCreatePaymentPage();
    }

    @And("User creates FED payment with amount {string} and beneficiary {string}")
    public void user_creates_fed_payment(String amount, String beneficiary) {
        Log.info("Step: Creating FED payment — Amount: " + amount + " | Beneficiary: " + beneficiary);
        paymentPage.createFedPayment(amount, beneficiary);
        paymentPage.clickSubmit();
    }

    @Then("Payment should be created successfully")
    public void payment_should_be_created_successfully() {
        if (!paymentPage.isSuccessMessageDisplayed()) {
            throw new AssertionError("Payment success message is NOT displayed — FED payment creation failed");
        }

        String message = paymentPage.getSuccessMessage();
        Log.info("Step: Payment created. Message: " + message);

        if (!message.toLowerCase().contains("success")) {
            throw new AssertionError("Expected success confirmation but got: '" + message + "'");
        }

        String refNumber = paymentPage.getReferenceNumber();
        Log.info("Payment Reference Number: " + refNumber);
    }
}
