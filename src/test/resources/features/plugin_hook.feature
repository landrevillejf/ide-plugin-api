Feature: Plugin Hook Service
  As a plugin developer
  I want to register lifecycle hooks
  So that I can respond to plugin lifecycle events

  Scenario: Plugin can register and execute hooks
    When plugin registers hook for "POST_INIT"
    And hook is triggered
    Then hook callback should be executed