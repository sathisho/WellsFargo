package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.Log;

import java.time.Duration;

/**
 * Page Object for FED Payment creation in WIRES application.
 * Locators are externalized in resources/locators/PaymentPage.properties.
 */
public class PaymentPage {
    private WebDriver driver;
    private WebDriverWait wait;

    // --- Locators (mapped from PaymentPage.properties) ---
    private By paymentTypeDropdown = By.xpath("//select[@id='paymentType']");
    private By amountField         = By.xpath("//input[@id='amount']");
    private By beneficiaryField    = By.xpath("//input[@id='beneficiary']");
    private By submitButton        = By.xpath("//button[@id='submitPayment']");
    private By successMessage      = By.xpath("//div[contains(@class,'alert-success')]");
    private By createPaymentLink   = By.xpath("//a[contains(text(),'Create Payment')]");

    public PaymentPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    /**
     * Navigate to the Create Payment page via the left-nav or menu link.
     */
    public void openCreatePaymentPage() {
        Log.info("Navigating to Create Payment page");
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(createPaymentLink));
        link.click();
        Log.info("Create Payment page opened");
    }

    /**
     * Fill in FED payment details and submit.
     *
     * @param amount      the payment amount
     * @param beneficiary the beneficiary name
     */
    public void createFedPayment(String amount, String beneficiary) {
        Log.info("Creating FED payment | Amount: " + amount + " | Beneficiary: " + beneficiary);

        // Select payment type as FED
        WebElement typeDropdown = wait.until(ExpectedConditions.visibilityOfElementLocated(paymentTypeDropdown));
        Select select = new Select(typeDropdown);
        select.selectByVisibleText("FED");
        Log.info("Selected payment type: FED");

        // Enter amount
        WebElement amountEl = wait.until(ExpectedConditions.visibilityOfElementLocated(amountField));
        amountEl.clear();
        amountEl.sendKeys(amount);
        Log.info("Entered amount: " + amount);

        // Enter beneficiary
        WebElement beneficiaryEl = wait.until(ExpectedConditions.visibilityOfElementLocated(beneficiaryField));
        beneficiaryEl.clear();
        beneficiaryEl.sendKeys(beneficiary);
        Log.info("Entered beneficiary: " + beneficiary);
    }

    /**
     * Click the Submit button to create the payment.
     */
    public void clickSubmit() {
        Log.info("Clicking Submit button");
        WebElement submit = wait.until(ExpectedConditions.elementToBeClickable(submitButton));
        submit.click();
        Log.info("Submit button clicked");
    }

    /**
     * Verify the success message is displayed after payment creation.
     *
     * @return the success message text
     */
    public String getSuccessMessage() {
        WebElement alert = wait.until(ExpectedConditions.visibilityOfElementLocated(successMessage));
        String message = alert.getText();
        Log.info("Success message displayed: " + message);
        return message;
    }

    /**
     * Wait for the success message to be visible and verify it exists.
     *
     * @return true if the success message is displayed
     */
    public boolean isSuccessMessageDisplayed() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(successMessage)).isDisplayed();
        } catch (Exception e) {
            Log.error("Success message NOT displayed: " + e.getMessage());
            return false;
        }
    }
}
