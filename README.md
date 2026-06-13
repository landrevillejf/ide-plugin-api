# IDE Plugin API

[![Version](https://img.shields.io/badge/version-1.3.0-RC-1-blue.svg)](https://github.com/landrevillejf/ide-plugin-api)
[![Java](https://img.shields.io/badge/Java-21+-orange.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)

[![Build Status](https://github.com/landrevillejf/ide-plugin-api/actions/workflows/release.yml/badge.svg)](https://github.com/landrevillejf/ide-plugin-api/actions/workflows/release.yml)
[![Tests](https://img.shields.io/badge/tests-1134%20total%2C%201131%20passed%2C%203%20failed%2C%2036%20skipped-green.svg)](https://github.com/landrevillejf/ide-plugin-api/actions)
[![Coverage](https://img.shields.io/badge/coverage-72%25-green.svg)](https://github.com/landrevillejf/ide-plugin-api/actions/workflows/release.yml)

[![Mutation Tests](https://img.shields.io/badge/mutation%20tests-36%20survived%2F%20134%20mutations%20total-yellow.svg)](https://github.com/landrevillejf/ide-plugin-api/actions)
[![Cucumber Tests](https://img.shields.io/badge/cucumber-36%2F36%20passed-green.svg)](https://github.com/landrevillejf/ide-plugin-api/actions)
[![JaCoCo](https://img.shields.io/badge/JaCoCo-0.8.12-blue.svg)](https://www.eclemma.org/jacoco/)

[![PITest](https://img.shields.io/badge/PITest-1.19.0--rc.3-blue.svg)](https://pitest.org/)
[![CodeQL](https://github.com/landrevillejf/ide-plugin-api/actions/workflows/codeql.yml/badge.svg)](https://github.com/landrevillejf/ide-plugin-api/actions/workflows/codeql.yml)
[![OWASP Dependency Check](https://img.shields.io/badge/OWASP%20DC-12.1.1-blue.svg)](https://jeremylong.github.io/DependencyCheck/)

[![SonarQube](https://img.shields.io/badge/SonarQube-5.0.0-blue.svg)](https://www.sonarqube.org/)
[![Gradle](https://img.shields.io/badge/Gradle-9.3-blue.svg)](https://gradle.org/)
[![GitHub release](https://img.shields.io/github/v/release/landrevillejf/ide-plugin-api)](https://github.com/landrevillejf/ide-plugin-api/releases)
[![GitHub commits](https://img.shields.io/github/commit-activity/m/landrevillejf/ide-plugin-api)](https://github.com/landrevillejf/ide-plugin-api/commits/main)

A comprehensive, modular plugin system for the IDE that allows developers to extend the IDE with custom functionality, UI components, and services.

## 📋 Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [Plugin Development](#plugin-development)
- [Plugin Services](#plugin-services)
- [UI Integration](#ui-integration)
- [Event System](#event-system)
- [API Reference](#api-reference)
- [Examples](#examples)
- [Best Practices](#best-practices)
- [Troubleshooting](#troubleshooting)

## Overview

The IDE Plugin API provides a robust framework for extending the IDE with custom plugins. Built with modularity and type safety in mind, it offers:

- **Full UI Integration** - Add tabs, panels, toolbars, and menu items
- **Service-Oriented Architecture** - Access IDE services and register your own
- **Event-Driven Communication** - Publish and subscribe to events
- **Lifecycle Management** - Full control over plugin initialization and cleanup
- **Permission System** - Fine-grained access control
- **Async Task Execution** - Background task support
- **Data Persistence** - Store plugin data securely
- **Dependency Resolution** - Manage plugin dependencies

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        IDE                             │
├─────────────────────────────────────────────────────────────┤
│                    Plugin Manager                           │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐           │
│  │   Plugin    │ │   Plugin    │ │   Plugin    │           │
│  │     A       │ │     B       │ │     C       │           │
│  └─────────────┘ └─────────────┘ └─────────────┘           │
│         │              │              │                     │
│         ▼              ▼              ▼                     │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                Plugin Context                        │   │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌────────┐  │   │
│  │  │ Services │ │ EventBus │ │ Registry │ │  Data  │  │   │
│  │  └──────────┘ └──────────┘ └──────────┘ └────────┘  │   │
│  └─────────────────────────────────────────────────────┘   │
│                          │                                  │
│                          ▼                                  │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                    IDE Core                          │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

## Getting Started

### Prerequisites

- Java 21 or higher
- IDE 1.0.0 or higher
- Gradle 8.0+ (for building)

### Creating Your First Plugin

#### 1. Project Structure

```
my-plugin/
├── build.gradle
├── src/
│   └── main/
│       ├── java/
│       │   └── com/example/myplugin/
│       │       └── MyPlugin.java
│       └── resources/
│           └── META-INF/
│               └── plugin.properties
└── README.md
```

#### 2. Plugin Class

```java
package com.example.myplugin;

import com.protonmail.landrevillejf.IDE.plugin.*;
import com.protonmail.landrevillejf.IDE.plugin.ui.UIComponentBuilder;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;

@Slf4j
public class MyPlugin extends AbstractPlugin implements Plugin, MenuProvider {

    private static final String PLUGIN_ID = "my-plugin";
    private static final String PLUGIN_NAME = "My Plugin";
    private static final String PLUGIN_VERSION = "1.0.0";
    
    private PluginContext context;
    private UIComponentBuilder uiBuilder;

    public MyPlugin() {
        super(PLUGIN_ID, PLUGIN_NAME, PLUGIN_VERSION, 
              "My awesome plugin description", "Your Name");
    }

    @Override
    public void initialize(PluginContext context) {
        this.context = context;
        this.uiBuilder = new UIComponentBuilder(context, PLUGIN_ID);
        
        // Create your UI
        JPanel panel = new JPanel();
        panel.add(new JLabel("Hello from MyPlugin!"));
        
        // Register UI component
        uiBuilder.addBottomPanel("my-plugin-panel", "My Plugin", panel)
                 .registerAll();
        
        context.logInfo("MyPlugin initialized");
    }

    @Override
    public List<JMenuItem> getMenuItems() {
        List<JMenuItem> items = new ArrayList<>();
        JMenu menu = new JMenu("My Plugin");
        
        JMenuItem actionItem = new JMenuItem("Do Something");
        actionItem.addActionListener(e -> doSomething());
        menu.add(actionItem);
        
        items.add(menu);
        return items;
    }

    private void doSomething() {
        context.showNotification("MyPlugin", "Doing something!");
    }

    @Override
    public PluginDescriptor getDescriptor() {
        PluginDescriptor desc = new PluginDescriptor();
        desc.setId(PLUGIN_ID);
        desc.setName(PLUGIN_NAME);
        desc.setVersion(PLUGIN_VERSION);
        desc.setMainClass(getClass().getName());
        desc.setAuthor("Your Name");
        desc.setDescription("My awesome plugin description");
        return desc;
    }
}
```

#### 3. Build Configuration

```groovy
// build.gradle
plugins {
    id 'java'
}

group = 'com.example'
version = '1.0.0'

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    compileOnly 'com.protonmail.landrevillejf:plugin-api:1.0.0'
    compileOnly 'org.projectlombok:lombok:1.18.28'
    annotationProcessor 'org.projectlombok:lombok:1.18.28'
}

jar {
    manifest {
        attributes(
            'Plugin-Id': 'my-plugin',
            'Plugin-Version': '1.0.0',
            'Plugin-Main-Class': 'com.example.myplugin.MyPlugin',
            'Plugin-Required-Host-Version': '1.0.0'
        )
    }
}
```

#### 4. Plugin Properties

```properties
# META-INF/plugin.properties
plugin.id=my-plugin
plugin.name=My Plugin
plugin.version=1.0.0
plugin.main.class=com.example.myplugin.MyPlugin
plugin.author=Your Name
plugin.description=My awesome plugin description
plugin.required.host.version=1.0.0
```

## Plugin Development

### Plugin Lifecycle

```java
public interface Plugin {
    void initialize(PluginContext context);  // Called when plugin is loaded
    void enable();                            // Called when plugin is enabled
    void disable();                           // Called when plugin is disabled
    void shutdown();                          // Called when IDE closes
    PluginDescriptor getDescriptor();         // Returns plugin metadata
}
```

### Plugin Types

| Type | Interface | Description |
|------|-----------|-------------|
| **Standard** | `Plugin` | Basic plugin functionality |
| **Menu Provider** | `MenuProvider` | Adds menu items to the IDE |
| **Toolbar Provider** | `ToolBarProvider` | Adds toolbar buttons |
| **UI Component Provider** | `UIComponentProvider` | Provides UI components for registration |

### Plugin Configuration

```java
public class MyPlugin extends AbstractPlugin {
    
    @Override
    public void initialize(PluginContext context) {
        // Get configuration
        PluginConfig config = getConfig();
        boolean autoEnable = config.isAutoEnable();
        String setting = config.getSettingAsString("my.setting", "default");
        
        // Save settings
        config.setSetting("my.setting", "new value");
        config.enableFeature("my-feature");
    }
}
```

## Plugin Services

### Available Services

| Service | Description | How to Get |
|---------|-------------|------------|
| `PluginLoggingService` | Logging with plugin context | `context.getLoggingService()` |
| `PluginCacheService` | Cache plugin data | `context.getCacheService()` |
| `PluginNotificationService` | Show notifications | `context.getNotificationService()` |
| `PluginMetricsService` | Record plugin metrics | `context.getMetricsService()` |
| `PluginPermissionService` | Check permissions | `context.getPermissionService()` |
| `PluginAsyncTaskExecutor` | Execute async tasks | `context.getAsyncTaskExecutor()` |
| `PluginDataStore` | Persistent data storage | `context.getDataStore()` |
| `PluginResourceManager` | Manage plugin resources | `context.getResourceManager()` |
| `PluginDependencyResolver` | Resolve dependencies | `context.getDependencyResolver()` |
| `PluginUpdateService` | Check for updates | `context.getUpdateService()` |
| `PluginMonitoringService` | Monitor plugin health | `context.getMonitoringService()` |

### Using Services

```java
public class MyPlugin extends AbstractPlugin {
    private PluginAsyncTaskExecutor taskExecutor;
    
    @Override
    public void initialize(PluginContext context) {
        if (context instanceof ExtendedPluginContext) {
            ExtendedPluginContext extContext = (ExtendedPluginContext) context;
            
            // Get services
            taskExecutor = extContext.getAsyncTaskExecutor();
            PluginNotificationService notification = extContext.getNotificationService();
            PluginDataStore dataStore = extContext.getDataStore();
            
            // Execute async task
            taskExecutor.executeNamedTask(PLUGIN_ID, "My Task", () -> {
                // Do work here
                notification.notify(PLUGIN_ID, NotificationType.INFO, 
                                   Priority.NORMAL, "Task Complete");
            });
        }
    }
}
```

### Registering Custom Services

```java
// Register your own service for other plugins
context.registerService(MyService.class, new MyServiceImpl());

// Other plugins can get your service
MyService service = context.getService(MyService.class);
```

## UI Integration

### UI Component Types

| Type | Constant | Description |
|------|----------|-------------|
| IDE Tab | `IDE_TAB` | Tab in the main editor area |
| Bottom Panel | `BOTTOM_PANEL` | Panel in the bottom area |
| Left Sidebar | `LEFT_SIDEBAR` | Panel in the left sidebar |
| Right Sidebar | `RIGHT_SIDEBAR` | Panel in the right sidebar |
| Toolbar Button | `TOOLBAR_BUTTON` | Button in the main toolbar |
| Menu Item | `MENU_ITEM` | Item in the plugins menu |
| Dockable Panel | `DOCKABLE_PANEL` | Floating tool window |
| Status Bar Component | `STATUS_BAR_COMPONENT` | Component in the status bar |

### Adding UI Components

```java
// Add a bottom panel
uiBuilder.addBottomPanel("my-panel", "My Panel", panelComponent, icon)
         .registerAll();

// Add an IDE tab
uiBuilder.addTab("my-tab", "My Tab", tabComponent, icon)
         .registerAll();

// Add a toolbar button
uiBuilder.addComponent("my-button", UIComponent.ComponentType.TOOLBAR_BUTTON,
                       "Click Me", buttonComponent, icon)
         .registerAll();

// Add with custom order
uiBuilder.addComponent("my-panel", UIComponent.ComponentType.BOTTOM_PANEL,
                       "My Panel", panelComponent, icon, 10, true)
         .registerAll();
```

### Accessing IDE UI Components

```java
// Get the UIComponentAccessor service
UIComponentAccessor accessor = context.getService(UIComponentAccessor.class);

if (accessor != null) {
    // Select a tab by ID
    accessor.selectTabById("my-panel");
    
    // Open a file in the editor
    accessor.openFileInEditor("/path/to/file.java");
    
    // Show notification
    accessor.showNotification("My Plugin", "Action completed");
    
    // Show dialog
    boolean confirmed = accessor.showConfirmDialog("Confirm", "Are you sure?");
    
    // Get current project
    Project project = (Project) accessor.getCurrentProject();
}
```

### UIComponentAccessor Methods

```java
// Tab management
JTabbedPane getIdeTabPane();
JTabbedPane getBottomTabPane();
boolean selectTabById(String componentId);
boolean selectTabByTitle(String title);

// File operations
void openFileInEditor(String filePath);
void openFileAtLine(String filePath, int line);
void openFileAtMember(String filePath, String memberName);

// Project Explorer
void refreshProjectExplorer();
boolean selectFileInExplorer(String filePath);
File getSelectedFileInExplorer();
boolean createJavaClass(String className, String packageName);
boolean deleteSelectedFile();

// Structure Panel
boolean showStructure(String filePath);
boolean navigateToMember(String memberName);
void expandStructureAll();

// Build Manager
boolean executeBuildTask(String taskName);
boolean isBuildRunning();
boolean stopCurrentBuild();
String getCurrentBuildTool();

// Notifications
void showNotification(String message);
void showNotification(String title, String message);
void showErrorDialog(String message);
void showInfoDialog(String title, String message);
boolean showConfirmDialog(String title, String message);
```

## Event System

### Built-in Events

| Event | Description |
|-------|-------------|
| `ProjectOpenedEvent` | Project opened |
| `ProjectClosedEvent` | Project closed |
| `ProjectClosingEvent` | Project about to close |
| `FileSavedEvent` | File saved |
| `FileOpenedEvent` | File opened |
| `EditorEvents.ActiveEditorChangedEvent` | Active editor changed |
| `SelectTabEvent` | Request to select a tab |

### Publishing Events

```java
// Get event bus
EventBus eventBus = context.getEventBus();

// Publish custom event
eventBus.publish(new MyCustomEvent(data));
```

### Subscribing to Events

```java
// Subscribe to events
eventBus.subscribe(ProjectOpenedEvent.class, event -> {
    Project project = event.getProject();
    log.info("Project opened: {}", project.getName());
});

eventBus.subscribe(EditorEvents.ActiveEditorChangedEvent.class, event -> {
    String filePath = event.getFilePath();
    log.info("Editor changed to: {}", filePath);
});

// For custom events
eventBus.subscribe(MyCustomEvent.class, this::onMyCustomEvent);

private void onMyCustomEvent(MyCustomEvent event) {
    // Handle event
}

// Unsubscribe when done
eventBus.unsubscribe(MyCustomEvent.class, this::onMyCustomEvent);
```

### Creating Custom Events

```java
public class MyCustomEvent {
    private final String data;
    private final long timestamp;
    
    public MyCustomEvent(String data) {
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }
    
    public String getData() { return data; }
    public long getTimestamp() { return timestamp; }
}
```

## API Reference

### Core Interfaces

#### PluginContext

```java
public interface PluginContext {
    String getPluginId();
    File getPluginDataPath();
    void logInfo(String message);
    void logError(String message, Throwable t);
    void showNotification(String title, String message);
    EventBus getEventBus();
    ComponentRegistry getComponentRegistry();
    <T> T getService(Class<T> serviceClass);
    <T> void registerService(Class<T> serviceClass, T implementation);
    void unregisterService(Class<?> serviceClass);
}
```

#### ExtendedPluginContext

```java
public interface ExtendedPluginContext extends PluginContext {
    // Additional services
    PluginLoggingService getLoggingService();
    PluginCacheService getCacheService();
    PluginNotificationService getNotificationService();
    PluginMetricsService getMetricsService();
    PluginPermissionService getPermissionService();
    PluginAsyncTaskExecutor getAsyncTaskExecutor();
    PluginConfigurationValidator getConfigurationValidator();
    PluginHookService getHookService();
    PluginDataStore getDataStore();
    PluginResourceManager getResourceManager();
    PluginDependencyResolver getDependencyResolver();
    PluginUpdateService getUpdateService();
    PluginMonitoringService getMonitoringService();
}
```

#### PluginDescriptor

```java
public class PluginDescriptor {
    void setId(String id);
    void setName(String name);
    void setVersion(String version);
    void setMainClass(String mainClass);
    void setAuthor(String author);
    void setAuthorEmail(String email);
    void setDescription(String description);
    void setCategory(String category);
    void setRequiredHostVersion(String version);
    void setDependencies(List<String> dependencies);
}
```

### Service Interfaces

```java
// Async Task Executor
public interface PluginAsyncTaskExecutor {
    void executeTask(String pluginId, Runnable task);
    void executeTaskWithPriority(String pluginId, Runnable task, TaskPriority priority);
    void executeNamedTask(String pluginId, String taskName, Runnable task);
    CompletableFuture<T> submitTask(String pluginId, Callable<T> task);
    void cancelTask(String pluginId, String taskName);
}

// Data Store
public interface PluginDataStore {
    void store(String pluginId, String key, Object data);
    <T> T retrieve(String pluginId, String key, Class<T> dataClass);
    void delete(String pluginId, String key);
    Set<String> getKeys(String pluginId);
    void clear(String pluginId);
}

// Cache Service
public interface PluginCacheService {
    void put(String pluginId, String key, Object value, long ttlMillis);
    <T> T get(String pluginId, String key, Class<T> dataClass);
    boolean contains(String pluginId, String key);
    void remove(String pluginId, String key);
    void clear(String pluginId);
    void setMaxSize(String pluginId, int maxSize);
}
```

## Examples

### Complete Plugin Example

```java
@Slf4j
public class ExamplePlugin extends AbstractPlugin 
    implements Plugin, MenuProvider, ToolBarProvider {

    private static final String PLUGIN_ID = "example-plugin";
    private ExtendedPluginContext context;
    private UIComponentBuilder uiBuilder;
    private JButton runButton;
    private PluginDataStore dataStore;
    
    public ExamplePlugin() {
        super(PLUGIN_ID, "Example Plugin", "1.0.0",
              "An example plugin", "Your Name");
    }
    
    @Override
    public void initialize(PluginContext context) {
        if (context instanceof ExtendedPluginContext) {
            this.context = (ExtendedPluginContext) context;
            this.dataStore = this.context.getDataStore();
        }
        
        this.uiBuilder = new UIComponentBuilder(context, PLUGIN_ID);
        
        // Load saved data
        String savedData = dataStore.retrieve(PLUGIN_ID, "myData", String.class);
        
        // Create UI
        JPanel panel = createPanel();
        
        // Register UI
        uiBuilder.addBottomPanel("example-panel", "Example", panel)
                 .registerAll();
        
        // Subscribe to events
        context.getEventBus().subscribe(ProjectOpenedEvent.class, this::onProjectOpened);
        
        context.logInfo("ExamplePlugin initialized");
    }
    
    private JPanel createPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        runButton = new JButton("Run Action");
        runButton.addActionListener(e -> runAction());
        panel.add(runButton, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void runAction() {
        // Use async executor
        context.getAsyncTaskExecutor().executeNamedTask(PLUGIN_ID, "Heavy Task", () -> {
            try {
                // Simulate work
                Thread.sleep(2000);
                
                // Save result
                dataStore.store(PLUGIN_ID, "lastRun", System.currentTimeMillis());
                
                // Show notification
                context.showNotification("Example", "Task completed!");
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }
    
    private void onProjectOpened(ProjectOpenedEvent event) {
        Project project = event.getProject();
        context.showNotification("Example", "Project opened: " + project.getName());
    }
    
    @Override
    public List<JMenuItem> getMenuItems() {
        List<JMenuItem> items = new ArrayList<>();
        JMenuItem item = new JMenuItem("Example Action");
        item.addActionListener(e -> runAction());
        items.add(item);
        return items;
    }
    
    @Override
    public List<JButton> getButtonItems() {
        List<JButton> buttons = new ArrayList<>();
        JButton button = new JButton("Example");
        button.addActionListener(e -> runAction());
        buttons.add(button);
        return buttons;
    }
}
```

### Custom UI Component Example

```java
public class CustomUIComponent extends JPanel {
    private final JLabel statusLabel;
    
    public CustomUIComponent() {
        setLayout(new BorderLayout());
        
        statusLabel = new JLabel("Ready", SwingConstants.CENTER);
        add(statusLabel, BorderLayout.CENTER);
        
        JButton button = new JButton("Update");
        button.addActionListener(e -> updateStatus());
        add(button, BorderLayout.SOUTH);
    }
    
    private void updateStatus() {
        statusLabel.setText("Updated at: " + new Date());
    }
}

// Register it
uiBuilder.addBottomPanel("custom-component", "Custom", new CustomUIComponent())
         .registerAll();
```

### Build Task Integration Example

```java
// Add a custom build task
UIComponentAccessor accessor = context.getService(UIComponentAccessor.class);

accessor.addCustomBuildTask("My Task", "Run my custom task", 
    "Custom", true, true, true);

// Execute build
accessor.executeBuildTask("clean");
accessor.executeBuildTask("build", new String[]{"-x", "test"});

// Monitor build progress
accessor.addBuildListener(new BuildListener() {
    @Override
    public void onBuildStarted(String taskName) {
        log.info("Build started: {}", taskName);
    }
    
    @Override
    public void onBuildCompleted(String taskName, long duration) {
        log.info("Build completed in {}ms", duration);
    }
    
    @Override
    public void onBuildFailed(String taskName, String errorMessage) {
        log.error("Build failed: {}", errorMessage);
    }
});
```

## Best Practices

### 1. Plugin Structure

```
com.example.myplugin/
├── plugin/
│   └── MyPlugin.java
├── service/
│   └── MyService.java
├── ui/
│   └── MyPanel.java
├── model/
│   └── MyModel.java
└── resources/
    └── META-INF/
        └── plugin.properties
```

### 2. Error Handling

```java
try {
    // Plugin logic
} catch (Exception e) {
    log.error("Error in plugin", e);
    context.logError("Failed to perform action", e);
    
    if (notificationService != null) {
        notificationService.notify(PLUGIN_ID, 
            PluginNotificationService.NotificationType.ERROR,
            PluginNotificationService.Priority.HIGH,
            "Error", e.getMessage());
    }
}
```

### 3. Threading

```java
// Always use async executor for long-running tasks
taskExecutor.executeNamedTask(PLUGIN_ID, "Heavy Task", () -> {
    // Heavy computation here
    
    // Update UI on EDT
    SwingUtilities.invokeLater(() -> {
        // UI updates
    });
});

// Don't block the EDT
SwingUtilities.invokeLater(() -> {
    // UI operations
});
```

### 4. Resource Cleanup

```java
@Override
public void shutdown() {
    // Clean up resources
    if (timer != null) {
        timer.stop();
    }
    
    // Unregister listeners
    eventBus.unsubscribe(MyEvent.class, this::onMyEvent);
    
    // Clear cache
    if (cacheService != null) {
        cacheService.clear(PLUGIN_ID);
    }
    
    super.shutdown();
}
```

### 5. Configuration Management

```java
// Use PluginConfig for settings
PluginConfig config = getConfig();

// Save user preferences
config.setSetting("my.setting", value);

// Load with default
String setting = config.getSettingAsString("my.setting", "default");

// Save configuration
config.enableFeature("my-feature");
```

## Troubleshooting

### Common Issues

| Issue | Solution |
|-------|----------|
| Plugin doesn't load | Check JAR is in `plugins/` directory and manifest is correct |
| UI components not showing | Verify `uiBuilder.registerAll()` is called |
| Services not available | Cast PluginContext to ExtendedPluginContext |
| Events not received | Check subscription is after event bus initialization |
| Data not persisting | Ensure `dataStore.store()` is called and directory is writable |
| ClassNotFoundException | Check plugin dependencies and classpath |

### Debugging

```java
// Enable debug logging
config.setSetting("plugin.debug", "true");

// Log plugin state
log.debug("Plugin state: {}", pluginState);
context.logInfo("Current project: {}", currentProject);

// Check service availability
if (context instanceof ExtendedPluginContext) {
    PluginLoggingService logging = ((ExtendedPluginContext) context).getLoggingService();
    if (logging != null) {
        logging.setLogLevel(PLUGIN_ID, PluginLoggingService.LogLevel.DEBUG);
    }
}
```

### Log File Location

```
~/.IDE/logs/application.log
```

## API Version Compatibility

| Plugin API Version | IDE Version | Changes |
|--------------------|-------------|---------|
| 1.0.0              | 1.0.0+      | Initial release |
| 1.1.0              | 1.1.0+      | Added UIComponentAccessor |
| 1.2.0              | 1.2.0+      | Added Build Manager integration |

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Open a Pull Request

## License

This project is licensed under the MIT License.

## Support

- **Issues**: [GitHub Issues](https://github.com/yourusername/swing-ide/issues)
- **Email**: support@IDE.com
- **Documentation**: [docs.IDE.com](https://docs.IDE.com)

---

**Version**: 3.0.0  
**Last Updated**: 2026-06-05  
**Maintainer**: Jean-Francois Landreville, landrevilejf@protonmail.com