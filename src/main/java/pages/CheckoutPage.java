package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class CheckoutPage {
    private WebDriver driver;
    private WebDriverWait wait;

    // Search
    private By searchBox = By.cssSelector("input[placeholder='Search']");
    private By searchIcon = By.xpath("//button[@class='btn btn-default btn-lg']");

    // Product actions
    private By addToCartButton = By.xpath("//span[normalize-space()='Add to Cart']");

    // Success message
    private By successMessage = By.cssSelector(".alert.alert-success");

    // Cart
    private By cartItemsList = By.xpath("//span[@id='cart-total']");
    private By checkoutButton = By.xpath("//strong[normalize-space()='Checkout']");

    // Checkout warning
    private By checkoutWarningMessage = By.cssSelector(".alert.alert-danger");

    public CheckoutPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public void enterSearchText(String text) {
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(searchBox));
        field.clear();
        field.sendKeys(text);
    }

    public void clickSearchIcon() {
        wait.until(ExpectedConditions.elementToBeClickable(searchIcon)).click();
    }

    public void clickAddToCart() {
        wait.until(ExpectedConditions.elementToBeClickable(addToCartButton)).click();
    }

    public String getSuccessMessage() {
        WebElement alert = wait.until(ExpectedConditions.visibilityOfElementLocated(successMessage));
        return alert.getText();
    }

    public boolean isSuccessMessageDisplayed() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(successMessage)).isDisplayed();
    }

    public void clickCartItemsList() {
        wait.until(ExpectedConditions.elementToBeClickable(cartItemsList)).click();
    }

    public void clickCheckoutButton() {
        wait.until(ExpectedConditions.elementToBeClickable(checkoutButton)).click();
    }

    public String getCheckoutWarningMessage() {
        WebElement alert = wait.until(ExpectedConditions.visibilityOfElementLocated(checkoutWarningMessage));
        return alert.getText();
    }

    public boolean isCheckoutWarningDisplayed() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(checkoutWarningMessage)).isDisplayed();
    }
}
