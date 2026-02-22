@WIRES @Login
Feature: WIRES Application Login
  As a WIRES user
  I want to login to the application
  So that I can access payment features

  @Smoke
  Scenario: Successful login with valid credentials
    Given User logs into WIRES application
    Then User should be logged in successfully
