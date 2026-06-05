# language: en
@plugin-descriptor
Feature: Plugin Descriptor
  As a plugin developer
  I want to create and manage plugin descriptors
  So that plugin metadata is properly tracked

  # ── Creation ──

  Scenario: Create a minimal plugin descriptor
    When I create a descriptor with id "my-plugin" name "My Plugin" version "1.0.0" mainClass "com.example.MyPlugin" description "A test plugin" author "Dev"
    Then the descriptor id should be "my-plugin"
    And the descriptor name should be "My Plugin"
    And the descriptor version should be "1.0.0"
    And the descriptor mainClass should be "com.example.MyPlugin"
    And the descriptor description should be "A test plugin"
    And the descriptor author should be "Dev"

  # ── Defaults ──

  Scenario: Minimal descriptor has default values
    When I create a descriptor with id "p1" name "P1" version "1.0" mainClass "Main" description "desc" author "me"
    Then the descriptor category should be "General"
    And the descriptor requiredHostVersion should be "1.0.0"
    And the descriptor should not be enabled by default
    And the descriptor should not auto-start

  # ── Copy ──

  Scenario: Copy a plugin descriptor
    Given a descriptor with id "original" name "Original" version "2.0" mainClass "Main" description "desc" author "author"
    When I copy the descriptor
    Then the copy should have id "original"
    And the copy should have name "Original"
    And the copy should have version "2.0"

  # ── Capabilities ──

  Scenario: Set plugin capabilities
    Given a descriptor with id "cap-test" name "CapTest" version "1.0" mainClass "Main" description "desc" author "dev"
    When I set providesMenu to true
    And I set providesToolbar to true
    And I set requiresNetwork to true
    Then the descriptor should provide menu
    And the descriptor should provide toolbar
    And the descriptor should require network

