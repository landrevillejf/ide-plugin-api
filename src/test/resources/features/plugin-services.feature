Feature: Plugin Logging Service
  As a plugin developer
  I want to use a centralized logging service
  So that I can easily log plugin events

  Scenario: Plugin can log messages at different levels
    When plugin logs an info message "Plugin started"
    And plugin logs a debug message "Debugging information"
    And plugin logs an error message "Error occurred"
    Then all messages should be recorded

  Scenario: Plugin can retrieve recent logs
    When plugin logs 5 messages
    And plugin requests recent logs
    Then plugin should receive all logged messages

  Scenario: Plugin can clear logs
    When plugin logs some messages
    And plugin clears all logs
    Then no logs should remain

