Feature: Login and Navigation functionality

  Scenario: Verify Login page is displayed correctly
    Given User is on Login page
    Then User should see the Login page with email and password fields

  Scenario: Login with invalid credentials shows error
    Given User is on Login page
    When User enters username "invalid@email.com" and password "wrongpassword"
    And User clicks on Login button
    Then User should see an error message "Warning: No match for E-Mail Address and/or Password."

  Scenario: Search for a product and verify results
    Given User is on Home page
    When User searches for "iphone"
    Then User should see search results containing "iPhone"

  Scenario: Navigate to Forgotten Password page
    Given User is on Login page
    When User clicks on Forgotten Password link
    Then User should be on the Forgotten Password page

  Scenario: Verify homepage displays featured products
    Given User is on Home page
    Then User should see featured products on the homepage
