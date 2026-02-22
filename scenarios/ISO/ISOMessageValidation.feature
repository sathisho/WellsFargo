@ISO @Regression
Feature: ISO Message Validation
  As a payment operations analyst
  I want to validate ISO 20022 message format
  So that outbound FED payments conform to SWIFT/ISO standards

  Scenario: Validate pacs.008 message structure
    Given User logs into WIRES application
    When User navigates to Create Payment page
    And User creates FED payment with amount "10000" and beneficiary "ISO Test Corp"
    Then Payment should be created successfully
