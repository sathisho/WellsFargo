package com.wellsfargo.pages;

import com.wellsfargo.config.LocatorRepository;
import com.wellsfargo.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import utils.Log;

/**
 * Page Object for WIRES Payment Creation.
 * Supports FED, SWIFT, and other payment types.
 * All locators externalized in resources/web/locators/PaymentPage.properties.
 */
public class WiresPaymentPage {

    private final WebDriver driver;

    // Locators loaded from PaymentPage.properties
    private By createPaymentLink() {
        return By.xpath(LocatorRepository.getLocator("PaymentPage", "create.payment.link"));
    }

    private By paymentTypeDropdown() {
        return By.xpath(LocatorRepository.getLocator("PaymentPage", "payment.type.dropdown"));
    }

    private By amountField() {
        return By.xpath(LocatorRepository.getLocator("PaymentPage", "amount.field"));
    }

    private By beneficiaryField() {
        return By.xpath(LocatorRepository.getLocator("PaymentPage", "beneficiary.field"));
    }

    private By submitButton() {
        return By.xpath(LocatorRepository.getLocator("PaymentPage", "submit.button"));
    }

    private By successMessage() {
        return By.xpath(LocatorRepository.getLocator("PaymentPage", "success.message"));
    }

    private By referenceNumber() {
        return By.xpath(LocatorRepository.getLocator("PaymentPage", "reference.number"));
    }

    public WiresPaymentPage(WebDriver driver) {
        this.driver = driver;
    }

    /**
     * Navigate to Create Payment page.
     */
    public void openCreatePaymentPage() {
        Log.info("Navigating to Create Payment page");
        WaitUtils.waitForClickable(driver, createPaymentLink()).click();
        Log.info("Create Payment page opened");
    }

    /**
     * Create a FED payment with specified amount and beneficiary.
     */
    public void createFedPayment(String amount, String beneficiary) {
        Log.info("Creating FED Payment — Amount: " + amount + " | Beneficiary: " + beneficiary);

        // Select payment type
        WebElement dropdown = WaitUtils.waitForVisible(driver, paymentTypeDropdown());
        new Select(dropdown).selectByVisibleText("FED");
        Log.info("Selected payment type: FED");

        // Enter amount
        WebElement amountEl = WaitUtils.waitForVisible(driver, amountField());
        amountEl.clear();
        amountEl.sendKeys(amount);

        // Enter beneficiary
        WebElement beneficiaryEl = WaitUtils.waitForVisible(driver, beneficiaryField());
        beneficiaryEl.clear();
        beneficiaryEl.sendKeys(beneficiary);

        Log.info("Payment details entered");
    }

    /**
     * Click Submit to create the payment.
     */
    public void clickSubmit() {
        Log.info("Submitting payment");
        WaitUtils.waitForClickable(driver, submitButton()).click();
        Log.info("Payment submitted");
    }

    /**
     * Verify the success message is displayed.
     */
    public boolean isSuccessMessageDisplayed() {
        try {
            WebElement msg = WaitUtils.waitForVisible(driver, successMessage());
            return msg.isDisplayed();
        } catch (Exception e) {
            Log.error("Success message not displayed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get the success message text.
     */
    public String getSuccessMessage() {
        WebElement msg = WaitUtils.waitForVisible(driver, successMessage());
        String text = msg.getText();
        Log.info("Success message: " + text);
        return text;
    }

    /**
     * Get the payment reference number after successful creation.
     */
    public String getReferenceNumber() {
        try {
            WebElement ref = WaitUtils.waitForVisible(driver, referenceNumber());
            String refNum = ref.getText();
            Log.info("Payment Reference: " + refNum);
            return refNum;
        } catch (Exception e) {
            Log.warn("Reference number not found: " + e.getMessage());
            return "N/A";
        }
    }
}
