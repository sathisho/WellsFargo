@Regression @ECommerce
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

  @Smoke
  Scenario: Add MacBook to cart and proceed to checkout
    Given User is on Home page
    When User enters "macbook" in the search box
    And User clicks on the search icon
    And User clicks on Add to Cart button
    Then User should see the success message "Success: You have added MacBook to your shopping cart!"
    When User clicks on the cart items list
    And User clicks on the Checkout button
    Then User should see the checkout warning message "Products marked with *** are not available in the desired quantity or not in stock!"

  @Smoke
  Scenario: Add Canon EOS 5D to cart and proceed to checkout
    Given User is on Home page
    When User enters "Canon EOS 5D" in the search box
    And User clicks on the search icon
    And User clicks on the product "Canon EOS 5D"
    And User selects option "1" from the product select dropdown
    And User clicks on Add to Cart button on the product page
    Then User should see the success message "Success: You have added Canon EOS 5D to your shopping cart!"
    When User clicks on the cart items list
    And User clicks on the Checkout button
    Then User should see the checkout warning message "Products marked with *** are not available in the desired quantity or not in stock!"

  @Smoke
  Scenario: Add Apple Cinema 30 to cart and proceed to checkout
    Given User is on Home page
    When User enters "Apple Cinema 30" in the search box
    And User clicks on the search icon
    And User clicks on the product "Apple Cinema 30"
    And User fills all required product options
    And User clicks on Add to Cart button on the product page
    Then User should see the success message "Success: You have added Apple Cinema 30 to your shopping cart!"
    When User clicks on the cart items list
    And User clicks on the Checkout button
    Then User should see the checkout warning message "Products marked with *** are not available in the desired quantity or not in stock!"
