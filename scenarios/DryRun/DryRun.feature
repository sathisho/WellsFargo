@DryRun
Feature: Dry Run Verification
  Validates that all step definitions are properly mapped.

  Scenario: Verify login steps are bound
    Given User logs into WIRES application
    Then User should be logged in successfully

  Scenario: Verify payment steps are bound
    Given User logs into WIRES application
    When User navigates to Create Payment page
    And User creates FED payment with amount "100" and beneficiary "DryRun Test"
    Then Payment should be created successfully
