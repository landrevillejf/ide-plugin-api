# language: en
@plugin-status
Feature: Plugin Status lifecycle
  As a plugin developer
  I want plugin statuses to follow valid state transitions
  So that the plugin lifecycle is predictable and safe

  # ── isActive / isInactive ──

  Scenario: ENABLED status is active
    Given a plugin status ENABLED
    Then the status should be active

  Scenario: DISABLED status is inactive
    Given a plugin status DISABLED
    Then the status should be inactive

  Scenario: LOADED status is neither active nor inactive
    Given a plugin status LOADED
    Then the status should not be active
    And the status should not be inactive

  # ── toString ──

  Scenario Outline: Human-readable status descriptions
    Given a plugin status <status>
    Then the status description should be "<expected>"

    Examples:
      | status       | expected     |
      | ENABLED      | Enabled      |
      | DISABLED     | Disabled     |
      | LOADED       | Loaded       |
      | UNLOADED     | Unloaded     |
      | INITIALIZED  | Initialized  |
      | ERROR        | Error        |
      | RELOADING    | Reloading    |

  # ── Valid transitions ──

  Scenario Outline: Valid state transitions
    Given a plugin status <from>
    Then it should be allowed to transition to <to>

    Examples:
      | from         | to          |
      | LOADED       | ENABLED     |
      | LOADED       | DISABLED    |
      | ENABLED      | DISABLED    |
      | DISABLED     | ENABLED     |
      | INITIALIZED  | ENABLED     |
      | ENABLED      | ERROR       |
      | DISABLED     | ERROR       |

  # ── Invalid transitions ──

  Scenario Outline: Invalid state transitions
    Given a plugin status <from>
    Then it should not be allowed to transition to <to>

    Examples:
      | from         | to          |
      | SHUTDOWN     | ENABLED     |
      | SHUTDOWN     | DISABLED    |
      | UNLOADED     | ENABLED     |
      | ERROR        | ENABLED     |

