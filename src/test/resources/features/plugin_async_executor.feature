Feature: Plugin Async Task Executor
  As a plugin developer
  I want to execute tasks asynchronously
  So that long operations don't block the UI

  Scenario: Plugin can execute named tasks
    When plugin executes async named task "background-job"
    Then task should be created and executed

  Scenario: Plugin can schedule delayed tasks
    When plugin schedules task with 100ms delay
    Then task should execute after delay