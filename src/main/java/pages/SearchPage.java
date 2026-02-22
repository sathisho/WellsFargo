package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public class SearchPage {
    private WebDriver driver;
    private WebDriverWait wait;

    private By searchInput = By.name("search");
    private By searchButton = By.cssSelector("#search button");
    private By searchResultProducts = By.cssSelector(".product-layout .caption a");
    private By searchPageHeading = By.cssSelector("#content h1");

    public SearchPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public void enterSearchText(String text) {
        WebElement searchField = wait.until(ExpectedConditions.visibilityOfElementLocated(searchInput));
        searchField.clear();
        searchField.sendKeys(text);
    }

    public void clickSearchButton() {
        wait.until(ExpectedConditions.elementToBeClickable(searchButton)).click();
    }

    public void searchProduct(String productName) {
        enterSearchText(productName);
        clickSearchButton();
    }

    public List<String> getSearchResultProductNames() {
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(searchResultProducts));
        List<WebElement> products = driver.findElements(searchResultProducts);
        return products.stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
    }

    public boolean isProductInResults(String productName) {
        return getSearchResultProductNames().stream()
                .anyMatch(name -> name.toLowerCase().contains(productName.toLowerCase()));
    }

    public String getSearchPageHeading() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(searchPageHeading)).getText();
    }

    public String getPageTitle() {
        return driver.getTitle();
    }
}
