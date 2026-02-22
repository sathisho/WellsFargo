package stepdefinitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import pages.CheckoutPage;
import utils.DriverFactory;

public class CheckoutSteps {
    private WebDriver driver = DriverFactory.getDriver();
    private CheckoutPage checkoutPage = new CheckoutPage(driver);

    private void slowDown() {
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
    }

    @When("User enters {string} in the search box")
    public void user_enters_in_the_search_box(String productName) {
        slowDown();
        checkoutPage.enterSearchText(productName);
    }

    @And("User clicks on the search icon")
    public void user_clicks_on_the_search_icon() {
        slowDown();
        checkoutPage.clickSearchIcon();
    }

    @And("User clicks on Add to Cart button")
    public void user_clicks_on_add_to_cart_button() {
        slowDown();
        checkoutPage.clickAddToCart();
    }

    @Then("User should see the success message {string}")
    public void user_should_see_the_success_message(String expectedMessage) {
        slowDown();
        Assert.assertTrue(checkoutPage.isSuccessMessageDisplayed(),
                "Success message is not displayed");
        String actualMessage = checkoutPage.getSuccessMessage();
        Assert.assertTrue(actualMessage.contains(expectedMessage),
                "Expected success message to contain: '" + expectedMessage +
                "' but was: '" + actualMessage + "'");
    }

    @When("User clicks on the cart items list")
    public void user_clicks_on_the_cart_items_list() {
        slowDown();
        checkoutPage.clickCartItemsList();
    }

    @And("User clicks on the Checkout button")
    public void user_clicks_on_the_checkout_button() {
        slowDown();
        checkoutPage.clickCheckoutButton();
    }

    @Then("User should see the checkout warning message {string}")
    public void user_should_see_the_checkout_warning_message(String expectedMessage) {
        slowDown();
        Assert.assertTrue(checkoutPage.isCheckoutWarningDisplayed(),
                "Checkout warning message is not displayed");
        String actualMessage = checkoutPage.getCheckoutWarningMessage();
        Assert.assertTrue(actualMessage.contains(expectedMessage),
                "Expected checkout warning to contain: '" + expectedMessage +
                "' but was: '" + actualMessage + "'");
    }
}
