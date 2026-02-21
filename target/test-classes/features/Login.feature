Feature: Login functionality

  Scenario: Successful login with valid credentials
    Given User is on Login page
    When User enters username "testuser" and password "testpass"
    And User clicks on Login button
    Then User should see the homepage
