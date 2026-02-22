package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class LoginPage {
    private WebDriver driver;
    private WebDriverWait wait;

    private By usernameField = By.id("input-email");
    private By passwordField = By.id("input-password");
    private By loginButton = By.cssSelector("input[type='submit']");
    private By alertMessage = By.cssSelector(".alert.alert-danger");
    private By forgotPasswordLink = By.linkText("Forgotten Password");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public void enterUsername(String username) {
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(usernameField));
        field.clear();
        field.sendKeys(username);
    }

    public void enterPassword(String password) {
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(passwordField));
        field.clear();
        field.sendKeys(password);
    }

    public void clickLogin() {
        wait.until(ExpectedConditions.elementToBeClickable(loginButton)).click();
    }

    public String getPageTitle() {
        return driver.getTitle();
    }

    public String getAlertMessage() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(alertMessage)).getText();
    }

    public boolean isAlertDisplayed() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(alertMessage)).isDisplayed();
    }

    public boolean isLoginPageDisplayed() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(usernameField)).isDisplayed()
                && wait.until(ExpectedConditions.visibilityOfElementLocated(passwordField)).isDisplayed()
                && wait.until(ExpectedConditions.visibilityOfElementLocated(loginButton)).isDisplayed();
    }

    public void clickForgotPasswordLink() {
        wait.until(ExpectedConditions.elementToBeClickable(forgotPasswordLink)).click();
    }
}
