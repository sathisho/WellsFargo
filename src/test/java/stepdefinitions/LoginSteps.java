package stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import pages.LoginPage;
import config.ConfigReader;
import utils.DriverFactory;

public class LoginSteps {
    private WebDriver driver = DriverFactory.getDriver();
    private LoginPage loginPage = new LoginPage(driver);

    @Given("User is on Login page")
    public void user_is_on_login_page() {
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

    @Then("User should see the homepage")
    public void user_should_see_the_homepage() {
        Assert.assertTrue(loginPage.getPageTitle().contains("Home"));
    }
}
