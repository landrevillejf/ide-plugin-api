# language: en
@plugin-config
Feature: Plugin Configuration
  As a plugin developer
  I want to manage plugin configuration, settings, and feature toggles
  So that plugins can be customized at runtime

  Background:
    Given a new PluginConfig

  # ── Auto-enable ──

  Scenario: Auto-enable is disabled by default
    Then auto-enable should be false

  Scenario: Enable auto-enable
    When I set auto-enable to true
    Then auto-enable should be true

  # ── Settings ──

  Scenario: Set and retrieve a setting
    When I set setting "theme" to "dark"
    Then the setting "theme" should be "dark"

  Scenario: Retrieve missing setting returns default
    Then the setting "missing" with default "fallback" should be "fallback"

  Scenario: Check setting existence
    When I set setting "port" to "8080"
    Then the config should have setting "port"
    And the config should not have setting "host"

  Scenario: Remove a setting
    Given I set setting "key" to "value"
    When I remove setting "key"
    Then the config should not have setting "key"

  Scenario: Get setting as boolean
    When I set setting "debug" to "true"
    Then the boolean setting "debug" with default false should be true

  Scenario: Get setting as int
    When I set setting "port" to "3000"
    Then the int setting "port" with default 80 should be 3000

  # ── Features ──

  Scenario: Enable a feature
    When I enable feature "syntax-highlighting"
    Then the enabled features should contain "syntax-highlighting"

  Scenario: Disable a feature
    Given I enable feature "auto-save"
    When I disable feature "auto-save"
    Then the enabled features should not contain "auto-save"

  Scenario: Check if a feature is enabled
    When I enable feature "spell-check"
    Then the feature "spell-check" should be enabled

  Scenario: Check if a feature is not enabled
    Then the feature "unknown-feature" should not be enabled

