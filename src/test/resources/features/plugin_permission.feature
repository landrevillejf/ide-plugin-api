Feature: Plugin Permission Service
  As a plugin developer
  I want to manage plugin permissions
  So that I can control access to plugin resources

  Scenario: Plugin permissions can be granted and revoked
    When plugin is granted "filesystem.read" permission
    Then plugin should have "filesystem.read" permission
    And plugin removes "filesystem.read" permission
    Then plugin should not have "filesystem.read" permission

  Scenario: Plugin can check multiple permissions
    When plugin is granted "perm1" and "perm2" permissions
    Then plugin should have all required permissions