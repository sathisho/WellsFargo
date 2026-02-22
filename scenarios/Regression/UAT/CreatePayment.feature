@Regression @UAT @Payments
Feature: Create FED Payment in UAT
  As a WIRES application user in UAT environment
  I want to create a FED payment
  So that payment workflow is validated before production

  Scenario: Create FED Payment in UAT
    Given User logs into WIRES application
    When User navigates to Create Payment page
    And User creates FED payment with amount "2500" and beneficiary "UAT Beneficiary"
    Then Payment should be created successfully
