package stepdefinitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import config.ConfigReader;
import pages.PaymentPage;
import utils.DriverFactory;
import utils.Log;

/**
 * Step definitions for FED Payment creation workflow.
 * Follows enterprise coding standards:
 *   - No hardcoded locators (delegated to PaymentPage)
 *   - Parameterized steps for amount and beneficiary
 *   - Reusable login step via WIRES application URL
 *   - Assertions with clear failure messages
 */
public class PaymentSteps {
    private WebDriver driver = DriverFactory.getDriver();
    private PaymentPage paymentPage = new PaymentPage(driver);

    @Given("User logs into WIRES application")
    public void user_logs_into_wires_application() {
        String wiresUrl = ConfigReader.getProperty("wiresUrl");
        Log.info("Navigating to WIRES application: " + wiresUrl);
        driver.get(wiresUrl);
        Log.info("WIRES application loaded | Title: " + driver.getTitle());
    }

    @When("User navigates to Create Payment page")
    public void user_navigates_to_create_payment_page() {
        paymentPage.openCreatePaymentPage();
    }

    @And("User creates FED payment with amount {string} and beneficiary {string}")
    public void user_creates_fed_payment_with_amount_and_beneficiary(String amount, String beneficiary) {
        paymentPage.createFedPayment(amount, beneficiary);
        paymentPage.clickSubmit();
    }

    @Then("Payment should be created successfully")
    public void payment_should_be_created_successfully() {
        Assert.assertTrue(paymentPage.isSuccessMessageDisplayed(),
                "Payment success message is not displayed — FED payment creation may have failed");

        String successMsg = paymentPage.getSuccessMessage();
        Log.info("Payment created successfully. Message: " + successMsg);

        Assert.assertTrue(successMsg.toLowerCase().contains("success"),
                "Expected success confirmation but got: '" + successMsg + "'");
    }
}
