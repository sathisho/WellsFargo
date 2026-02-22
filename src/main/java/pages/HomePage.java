package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public class HomePage {
    private WebDriver driver;
    private WebDriverWait wait;

    private By featuredProducts = By.cssSelector(".product-layout .caption a");
    private By featuredHeading = By.xpath("//h3[contains(text(),'Featured')]");

    public HomePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public boolean isFeaturedSectionDisplayed() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(featuredHeading)).isDisplayed();
    }

    public List<String> getFeaturedProductNames() {
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(featuredProducts));
        List<WebElement> products = driver.findElements(featuredProducts);
        return products.stream()
                .map(WebElement::getText)
                .filter(text -> !text.isEmpty())
                .collect(Collectors.toList());
    }

    public boolean hasFeaturedProducts() {
        return !getFeaturedProductNames().isEmpty();
    }

    public String getPageTitle() {
        return driver.getTitle();
    }
}
