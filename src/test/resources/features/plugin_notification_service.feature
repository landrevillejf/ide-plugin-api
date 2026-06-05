Feature: Plugin Notification Service
  As a plugin developer
  I want to send notifications to users
  So that users are informed about plugin events

  Scenario: Plugin can send notifications
    When plugin sends notification with title "Success" and message "Operation completed"
    Then notification should be created successfully

  Scenario: Plugin can send priority notifications
    When plugin sends high-priority error notification
    Then notification should be marked as high priority