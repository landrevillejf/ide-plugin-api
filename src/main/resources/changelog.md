# Changelog

All notable changes to the IDE Plugin API project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/2.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Planned
- Additional UI component types
- Enhanced service registry
- Improved dependency resolution

## [1.3.0-RC1] - 2026-06-13

### Added
- **Build Manager Integration** - Execute Gradle/Maven tasks directly from plugins
    - `executeBuildTask(String taskName)` method
    - Build task monitoring with `BuildListener` interface
    - Support for custom build task registration
- **UIComponentAccessor Service** - Comprehensive UI manipulation API
    - Tab selection (`selectTabById`, `selectTabByTitle`)
    - File operations (`openFileInEditor`, `openFileAtLine`, `openFileAtMember`)
    - Project Explorer integration (`refreshProjectExplorer`, `selectFileInExplorer`)
    - Structure Panel navigation (`showStructure`, `navigateToMember`)
    - Dialog helpers (`showConfirmDialog`, `showErrorDialog`, `showInfoDialog`)
- **Extended UI Component Types**
    - `DOCKABLE_PANEL` - Floating tool windows
    - `STATUS_BAR_COMPONENT` - Status bar integration
- **Event System Enhancements**
    - `SelectTabEvent` for programmatic tab switching
    - `EditorEvents.ActiveEditorChangedEvent` for editor focus tracking
    - `ProjectClosingEvent` for pre-close cleanup hooks

### Changed
- Extended `PluginContext` to include service access for all core services
- Improved async task executor with priority support (`TaskPriority` enum)
- Enhanced notification service with priority levels (`Priority.NORMAL`, `Priority.HIGH`)
- Updated minimum Java requirement to Java 21
- Upgraded Gradle to 9.3

### Fixed
- Plugin service retrieval consistency across all service types
- Memory leak in event bus subscription cleanup
- Thread safety issues in PluginCacheService

### Security
- Added permission checks for file system access in UIComponentAccessor
- Input validation for file paths in `openFileInEditor` methods

## [1.2.0] - 2026-05-15

### Added
- **PluginAsyncTaskExecutor** - Background task execution with named task support
    - `executeNamedTask(String pluginId, String taskName, Runnable task)`
    - `submitTask(String pluginId, Callable<T> task)` returning CompletableFuture
    - `cancelTask(String pluginId, String taskName)`
- **PluginDataStore** - Persistent storage for plugin data
    - Type-safe storage with automatic serialization
    - `store()`, `retrieve()`, `delete()`, `clear()` methods
- **PluginCacheService** - In-memory caching with TTL support
    - Configurable max size per plugin
    - Automatic cache eviction
- **PluginMonitoringService** - Health and performance monitoring for plugins

### Changed
- ExtendedPluginContext now exposes all services directly
- Improved logging with structured log messages
- Enhanced error handling with detailed stack traces in debug mode

### Fixed
- Resource leak in PluginResourceManager
- Race condition during plugin initialization
- UI thread blocking issues in notifications

## [1.1.0] - 2026-04-20

### Added
- **MenuProvider Interface** - Add custom menu items to IDE menus
- **ToolBarProvider Interface** - Add custom toolbar buttons
- **UIComponentProvider Interface** - Provide UI components for registration
- **PluginConfigurationValidator** - Validate plugin configurations before loading
- **PluginHookService** - Register lifecycle hooks for plugin events
- **PluginUpdateService** - Check for plugin updates from remote repositories

### Changed
- Improved UIComponentBuilder API with fluent interface
- Better icon support for UI components
- Enhanced event bus with priority-based subscription
- Documentation restructured with table of contents

### Fixed
- Plugin dependency resolution ordering issues
- UI component registration race conditions
- Memory leak in event listener cleanup

## [1.0.0] - 2026-03-10

### Added
- **Initial Release** - First stable version of IDE Plugin API
- **Core Plugin Architecture**
    - `Plugin` interface with lifecycle methods (`initialize`, `enable`, `disable`, `shutdown`)
    - `AbstractPlugin` base class for convenient plugin implementation
    - `PluginContext` for plugin-environment interaction
    - `ExtendedPluginContext` with full service access
- **Plugin Management**
    - Plugin discovery and loading from JAR files
    - Plugin dependency resolution via `PluginDependencyResolver`
    - Plugin descriptor metadata (`PluginDescriptor` class)
- **Service Framework**
    - `PluginLoggingService` - Context-aware logging
    - `PluginNotificationService` - User notifications
    - `PluginMetricsService` - Performance metrics collection
    - `PluginPermissionService` - Fine-grained access control
    - `PluginResourceManager` - Resource lifecycle management
- **UI Integration**
    - Component registration via `ComponentRegistry`
    - `UIComponentBuilder` for easy UI creation
    - Support for IDE tabs, bottom panels, sidebars, and toolbar buttons
- **Event System**
    - `EventBus` for publish/subscribe communication
    - Built-in events: `ProjectOpenedEvent`, `ProjectClosedEvent`, `FileSavedEvent`, `FileOpenedEvent`
- **Configuration Management**
    - `PluginConfig` for plugin settings persistence
    - Feature flags and setting storage

### Documentation
- Complete plugin development guide
- API reference documentation
- Example plugins and best practices
- Troubleshooting section

### Testing
- 1134 total tests with 1131 passing
- 36 Cucumber integration tests
- 72% code coverage with JaCoCo
- Mutation testing with PITest

---

## Version History

| Version | Release Date | Key Features |
|---------|--------------|--------------|
| 1.3.0-RC1 | 2026-06-13 | Build Manager, UIComponentAccessor |
| 1.2.0 | 2026-05-15 | Async tasks, Data store, Caching |
| 1.1.0 | 2026-04-20 | Menu/Toolbar providers, Update service |
| 1.0.0 | 2026-03-10 | Initial stable release |

[unreleased]: https://github.com/landrevillejf/ide-plugin-api/compare/v1.3.0-RC1...HEAD
[1.3.0-RC1]: https://github.com/landrevillejf/ide-plugin-api/compare/v1.2.0...v1.3.0-RC1
[1.2.0]: https://github.com/landrevillejf/ide-plugin-api/compare/v1.1.0...v1.2.0
[1.1.0]: https://github.com/landrevillejf/ide-plugin-api/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/landrevillejf/ide-plugin-api/releases/tag/v1.0.0