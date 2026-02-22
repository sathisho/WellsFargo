package stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import pages.LoginPage;
import pages.SearchPage;
import pages.HomePage;
import config.ConfigReader;
import utils.DriverFactory;

import java.util.List;

public class LoginSteps {
    private WebDriver driver = DriverFactory.getDriver();
    private LoginPage loginPage = new LoginPage(driver);
    private SearchPage searchPage = new SearchPage(driver);
    private HomePage homePage = new HomePage(driver);

    @Given("User is on Login page")
    public void user_is_on_login_page() {
        driver.get("https://tutorialsninja.com/demo/index.php?route=account/login");
    }

    @Given("User is on Home page")
    public void user_is_on_home_page() {
        driver.get(ConfigReader.getProperty("baseUrl"));
    }

    @When("User enters username {string} and password {string}")
    public void user_enters_username_and_password(String username, String password) {
        loginPage.enterUsername(username);
        loginPage.enterPassword(password);
    }

    @When("User clicks on Login button")
    public void user_clicks_on_login_button() {
        loginPage.clickLogin();
    }

    @When("User searches for {string}")
    public void user_searches_for(String productName) {
        searchPage.searchProduct(productName);
    }

    @When("User clicks on Forgotten Password link")
    public void user_clicks_on_forgotten_password_link() {
        loginPage.clickForgotPasswordLink();
    }

    @Then("User should see the Login page with email and password fields")
    public void user_should_see_login_page_with_fields() {
        Assert.assertTrue(loginPage.isLoginPageDisplayed(),
                "Login page elements (email, password, login button) are not displayed");
        String title = loginPage.getPageTitle();
        Assert.assertTrue(title.contains("Account Login"),
                "Expected page title to contain 'Account Login' but was: " + title);
    }

    @Then("User should see an error message {string}")
    public void user_should_see_an_error_message(String expectedMessage) {
        Assert.assertTrue(loginPage.isAlertDisplayed(),
                "Error alert message is not displayed");
        String actualMessage = loginPage.getAlertMessage();
        Assert.assertTrue(actualMessage.contains(expectedMessage),
                "Expected error message to contain: '" + expectedMessage +
                "' but was: '" + actualMessage + "'");
    }

    @Then("User should see search results containing {string}")
    public void user_should_see_search_results_containing(String productName) {
        String pageTitle = searchPage.getPageTitle();
        Assert.assertTrue(pageTitle.contains("Search"),
                "Expected to be on Search results page but title was: " + pageTitle);
        Assert.assertTrue(searchPage.isProductInResults(productName),
                "Expected product '" + productName + "' not found in search results. " +
                "Found: " + searchPage.getSearchResultProductNames());
    }

    @Then("User should be on the Forgotten Password page")
    public void user_should_be_on_forgotten_password_page() {
        String title = driver.getTitle();
        Assert.assertTrue(title.contains("Forgot"),
                "Expected page title to contain 'Forgot' but was: " + title);
        String currentUrl = driver.getCurrentUrl();
        Assert.assertTrue(currentUrl.contains("account/forgotten"),
                "Expected URL to contain 'account/forgotten' but was: " + currentUrl);
    }

    @Then("User should see featured products on the homepage")
    public void user_should_see_featured_products() {
        Assert.assertTrue(homePage.isFeaturedSectionDisplayed(),
                "Featured section is not displayed on the homepage");
        Assert.assertTrue(homePage.hasFeaturedProducts(),
                "No featured products found on the homepage");
        List<String> products = homePage.getFeaturedProductNames();
        Assert.assertTrue(products.size() >= 2,
                "Expected at least 2 featured products but found: " + products.size() +
                " - Products: " + products);
    }
}
