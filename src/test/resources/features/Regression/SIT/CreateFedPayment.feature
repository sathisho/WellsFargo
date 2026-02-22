@Regression @SIT @Payments
Feature: Create FED Payment
  As a WIRES application user
  I want to create a FED payment
  So that funds are transferred successfully via Fedwire

  Scenario: Create FED Payment successfully
    Given User logs into WIRES application
    When User navigates to Create Payment page
    And User creates FED payment with amount "1000" and beneficiary "Automation Test"
    Then Payment should be created successfully
