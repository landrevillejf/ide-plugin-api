Feature: Plugin Metrics Service
  As a plugin developer
  I want to collect performance metrics
  So that I can monitor plugin performance

  Scenario: Plugin can record counters
    When plugin increments counter "requests" by 5
    And plugin increments counter "requests" by 3
    Then counter value should be 8

  Scenario: Plugin can measure operation time
    When plugin starts timer for operation
    And operation completes
    Then timer should record the duration