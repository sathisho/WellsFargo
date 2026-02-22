@Regression
Feature: Product Add to Cart and Checkout functionality

  @Smoke
  Scenario: Add iPhone to cart and proceed to checkout
    Given User is on Home page
    When User enters "iphone" in the search box
    And User clicks on the search icon
    And User clicks on Add to Cart button
    Then User should see the success message "Success: You have added iPhone to your shopping cart!"
    When User clicks on the cart items list
    And User clicks on the Checkout button
    Then User should see the checkout warning message "Products marked with *** are not available in the desired quantity or not in stock!"
