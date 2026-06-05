Feature: Plugin Monitoring Service
  As a plugin developer
  I want to monitor plugin health
  So that I can detect and respond to issues

  Scenario: Plugin can get health status
    When plugin requests health report
    Then health report should contain status, CPU usage, memory usage

  Scenario: Plugin can create and resolve alerts
    When plugin creates alert with severity "WARNING"
    Then alert should be created
    And plugin can resolve the alert