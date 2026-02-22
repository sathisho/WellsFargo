package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class CheckoutPage {
    private WebDriver driver;
    private WebDriverWait wait;

    // Search
    private By searchBox = By.cssSelector("input[placeholder='Search']");
    private By searchIcon = By.xpath("//button[@class='btn btn-default btn-lg']");

    // Product actions
    private By addToCartButton = By.xpath("//span[normalize-space()='Add to Cart']");
    private By productPageAddToCartButton = By.id("button-cart");

    // Product detail page options
    private By productSelectDropdown = By.cssSelector("select[id^='input-option']");
    private By productRadioOption = By.cssSelector("input[type='radio'][name^='option']");
    private By productCheckboxOption = By.cssSelector("input[type='checkbox'][name^='option']");
    private By productTextOption = By.cssSelector("input[type='text'][id^='input-option']");
    private By productTextareaOption = By.cssSelector("textarea[id^='input-option']");
    private By productDateOption = By.cssSelector("input[data-date-format][id^='input-option']");
    private By productTimeOption = By.xpath("//label[contains(text(),'Time')]/following::div[1]//input[@id]");
    private By productDateTimeOption = By.xpath("//label[contains(text(),'Date & Time')]/following::div[1]//input[@id]");
    private By productQuantity = By.id("input-quantity");

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

    // --- Product Detail Page Methods ---

    public void clickOnProduct(String productName) {
        By productLink = By.xpath("//a[contains(text(),'" + productName + "')]");
        wait.until(ExpectedConditions.elementToBeClickable(productLink)).click();
    }

    public void selectProductOption(String optionValue) {
        WebElement selectElement = wait.until(ExpectedConditions.visibilityOfElementLocated(productSelectDropdown));
        // Wait until the dropdown has more than 1 option (first is placeholder)
        wait.until(driver -> {
            Select sel = new Select(selectElement);
            return sel.getOptions().size() > 1;
        });
        Select select = new Select(selectElement);
        // Select the first non-placeholder option
        select.selectByIndex(select.getOptions().size() > 1 ? 1 : 0);
    }

    public void clickProductPageAddToCart() {
        wait.until(ExpectedConditions.elementToBeClickable(productPageAddToCartButton)).click();
    }

    public void fillAllRequiredProductOptions() {
        // Select dropdown - pick the first available option
        List<WebElement> selects = driver.findElements(productSelectDropdown);
        for (WebElement sel : selects) {
            Select select = new Select(sel);
            if (select.getOptions().size() > 1) {
                select.selectByIndex(1);
            }
        }

        // Radio button - select the first one
        List<WebElement> radios = driver.findElements(productRadioOption);
        if (!radios.isEmpty()) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", radios.get(0));
        }

        // Checkbox - check the first one
        List<WebElement> checkboxes = driver.findElements(productCheckboxOption);
        if (!checkboxes.isEmpty() && !checkboxes.get(0).isSelected()) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", checkboxes.get(0));
        }

        // Text input
        List<WebElement> textInputs = driver.findElements(productTextOption);
        for (WebElement input : textInputs) {
            if (input.getAttribute("value").isEmpty()) {
                input.clear();
                input.sendKeys("test");
            }
        }

        // Textarea
        List<WebElement> textareas = driver.findElements(productTextareaOption);
        for (WebElement textarea : textareas) {
            if (textarea.getAttribute("value").isEmpty()) {
                textarea.clear();
                textarea.sendKeys("test");
            }
        }

        // Date fields
        List<WebElement> dateInputs = driver.findElements(By.cssSelector("div.input-group.date input[id^='input-option']"));
        for (WebElement dateInput : dateInputs) {
            if (dateInput.getAttribute("value").isEmpty()) {
                ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].value = '2025-01-15';", dateInput);
            }
        }

        // Time fields
        List<WebElement> timeInputs = driver.findElements(By.cssSelector("div.input-group.time input[id^='input-option']"));
        for (WebElement timeInput : timeInputs) {
            if (timeInput.getAttribute("value").isEmpty()) {
                ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].value = '10:30';", timeInput);
            }
        }

        // Date & Time fields
        List<WebElement> dateTimeInputs = driver.findElements(By.cssSelector("div.input-group.datetime input[id^='input-option']"));
        for (WebElement dtInput : dateTimeInputs) {
            if (dtInput.getAttribute("value").isEmpty()) {
                ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].value = '2025-01-15 10:30';", dtInput);
            }
        }

        // Set minimum quantity if required (Apple Cinema 30" needs qty 2)
        WebElement qtyField = driver.findElement(productQuantity);
        qtyField.clear();
        qtyField.sendKeys("2");
    }
}
