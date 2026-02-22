@Regression @SIT @Payments
Feature: Create FED Payment in SIT
  As a WIRES application user in SIT environment
  I want to create a FED payment
  So that funds are transferred via Fedwire successfully

  Scenario: Create FED Payment successfully
    Given User logs into WIRES application
    When User navigates to Create Payment page
    And User creates FED payment with amount "1000" and beneficiary "Automation Test"
    Then Payment should be created successfully

  Scenario: Create FED Payment with large amount
    Given User logs into WIRES application
    When User navigates to Create Payment page
    And User creates FED payment with amount "5000000" and beneficiary "Corporate Client"
    Then Payment should be created successfully
