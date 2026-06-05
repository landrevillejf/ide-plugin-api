Feature: Plugin Data Store Service
  As a plugin developer
  I want to persist plugin data
  So that plugin state survives restarts

  Scenario: Plugin can store and retrieve data
    When plugin stores data with key "settings" and value "config"
    Then plugin can retrieve the stored data
    And data should match the original

  Scenario: Plugin can backup and restore data
    When plugin stores some data
    And plugin creates a backup
    And plugin clears all data
    And plugin restores from backup
    Then data should be restored