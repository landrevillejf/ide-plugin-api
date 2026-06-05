Feature: Plugin Caching Service
  As a plugin developer
  I want to cache data with configurable TTL
  So that I can improve performance

  Scenario: Plugin can cache and retrieve values
    When plugin caches value "test-value" with key "test-key"
    Then plugin can retrieve the cached value
    And the value matches the original

  Scenario: Plugin cache respects TTL
    When plugin caches value with TTL of 1000 milliseconds
    And wait for TTL to expire
    Then cached value should be expired