# IDE Plugin API — Architecture Documentation

Version 1.3.0-RC1 · Base package `com.protonmail.landrevillejf.ide.plugin`

---

## 1. Overview

The IDE Plugin API is a **service-oriented, event-driven plugin framework** for a
Swing-based IDE. It lets the host application:

- discover and load plugin JARs at runtime from a plugins directory,
- give each plugin an isolated context (data directory, event bus, services),
- manage a full plugin lifecycle (load → initialize → enable → disable → unload),
- expose 13 cross-cutting services (logging, cache, notifications, metrics, permissions,
  async execution, data store, hooks, resource manager, dependency resolver, update,
  monitoring, config validation),
- integrate plugin UI into the IDE shell (tabs, sidebars, toolbars, menus, status bar).

The framework depends on the IDE core libraries (`ide-core`, `common`, `IconManager`,
`project-manager` — local JARs) for `EventBus` and `ServiceRegistry` primitives.

---

## 2. High-Level Component View

```
┌──────────────────────────────────────────────────────────────────────┐
│                              Host IDE                                │
│   (Swing shell · core EventBus · core ServiceRegistry · UIComponentAccessor)
└───────────────▲──────────────────────────────────────────────────────┘
                │ owns
┌───────────────┴──────────────────────────────────────────────────────┐
│                       DefaultPluginManager                           │
│   • scans plugins/ directory for *.jar                               │
│   • reads MANIFEST.MF → PluginDescriptor                             │
│   • URLClassLoader per plugin → instantiates Plugin class            │
│   • drives lifecycle, enable/disable state, MenuProvider registry    │
└───┬───────────────────────┬────────────────────────────┬─────────────┘
    │ 1 per plugin          │                            │
┌───▼────────────────┐ ┌────▼─────────────────┐ ┌────────▼────────────┐
│ DefaultPluginContext│ │ DefaultExtended…     │ │  PluginEventBus     │
│ or DefaultExtended- │ │ Context              │ │  (per plugin) +     │
│ PluginContext       │ │ + ServiceLocator     │ │  core EventBus      │
│ • local services    │ │ + service cache      │ │  (application-wide) │
│ • data dir          │ │                      │ │                     │
└───┬─────────────────┘ └────┬─────────────────┘ └─────────────────────┘
    │ getService(...)        │ 13 typed getters
┌───▼────────────────────────▼─────────────────────────────────────────┐
│                        Service Layer (service/)                      │
│  Logging · Cache · Notification · Metrics · Permission · AsyncExec   │
│  DataStore · Hook · ResourceManager · DependencyResolver · Update    │
│  Monitoring · ConfigValidator        (impl/ = Default* impls)        │
└──────────────────────────────────────────────────────────────────────┘
```

---

## 3. Package Map

| Package | Responsibility | Key types |
|---|---|---|
| `plugin` (root) | Core contracts, lifecycle orchestration | `Plugin`, `AbstractPlugin`, `PluginManager`, `DefaultPluginManager`, `PluginContext`/`ExtendedPluginContext`, `PluginDescriptor`, `PluginConfig`, `PluginStatus`, `PluginEventBus`, `SecurePluginClassLoader`, `MenuProvider`, `ToolBarProvider`, `UIComponentAccessor`, `PluginServiceInitializer` |
| `plugin.events` | Event model (~36 event types) | `Event`, `BaseEvent`, `EventListener`, `ProjectOpenedEvent`, `FileSavedEvent`, `PluginEnabledEvent`, … |
| `plugin.service` | 13 service interfaces | `PluginLoggingService`, `PluginCacheService`, `PluginAsyncTaskExecutor`, … + `PluginServiceLocator` |
| `plugin.service.impl` | Default implementations | `DefaultPlugin*` (14 classes) |
| `plugin.ui` | UI integration | `UIComponent`, `UIComponentBuilder`, `ComponentRegistry`, `UIComponentProvider`, `listener/` |
| `plugin.utils` | Helpers | `PanelUtil`, `PluginServiceUtils` |

---

## 4. Core Abstractions

### 4.1 `Plugin` interface
The central contract (~50 methods, mostly defaulted). Logical groups:

- **Identity**: `getDescriptor()`, `getName()`, `getVersion()`, `getAuthor()`, `getCategory()`
- **Lifecycle**: `preInitialize()` → `initialize(ctx)` → `beforeEnable()` → `enable()` →
  `afterEnable()` → `beforeDisable()` → `disable()` → `afterDisable()` → `shutdown()`,
  plus `onStart()`/`onStop()`/`cleanup()` hooks
- **State**: `getState()`/`setState()`, `isEnabled()`
- **Configuration**: `getConfig()`, `getConfigurationSchema()`, `updateConfiguration()`,
  `saveSettings()`/`loadSettings()`
- **Validation**: `validate()`, `validateDependencies()`, `checkCompatibility()`,
  `getRequiredHostVersion()`
- **Events**: `handleEvent()`, `publishEvent()`, `getPublishedEvents()`, `getSubscribedEvents()`
- **Ops**: `getMetrics()`, `healthCheck()`, `onError()`, `onUpgrade()`
- **Security**: `getDefaultPermissions()`, `requiresIsolation()`

> Audit note: this interface is oversized; see AUDIT.md §4.3 M-8 for the planned split.

`AbstractPlugin` provides the base implementation with an internal descriptor,
settings/metrics maps, startup-time statistics, and a **private** `PluginEventBus`.

### 4.2 `PluginDescriptor`
Metadata POJO: id, name, version, main class, author, email, description, category,
required host version, dependencies. Populated from `MANIFEST.MF`
(`Plugin-Id`, `Plugin-Name`, `Plugin-Version`, `Plugin-Class`, …) at load time.

### 4.3 `PluginStatus` — state machine

```
UNLOADED → LOADED → INITIALIZED → ENABLED ⇄ DISABLED → SHUTTING_DOWN → SHUTDOWN
              ↘__________↗ (also LOADED→ENABLED directly)
   any of {ENABLED, DISABLED, INITIALIZED, LOADED, ENABLING, DISABLING, SHUTTING_DOWN}
   → ERROR → DISABLED (only recovery path)
```

Transitional states `ENABLING`/`DISABLING`/`RELOADING` exist; `canTransitionTo()`
enforces the table. Same-state transitions are allowed (idempotence).

### 4.4 Context hierarchy

```
PluginContext (interface)
 └── ExtendedPluginContext (interface, adds 13 service getters)
DefaultPluginContext (impl: local services map, data dir, logging, notifications)
 └── DefaultExtendedPluginContext (impl: PluginServiceLocator + service cache)
```

- `DefaultPluginContext` resolves services **local-first, then global** `ServiceRegistry`.
- Also holds the per-plugin `PluginEventBus` and a bridge reference to the application
  `EventBus` (from `ide-core`).
- `PluginServiceInitializer.createExtendedPluginContext()` is the factory used by hosts
  that want the full service stack; it installs a `StubPluginServiceLocator` unless a
  custom `PluginServiceLocator` is registered.

### 4.5 `PluginManager` / `DefaultPluginManager`
Orchestrator. Responsibilities: directory scanning, JAR/class loading, context creation,
lifecycle dispatch, enable-state bookkeeping, `MenuProvider` registration into the global
`ServiceRegistry`, event publication (`PluginLoadedEvent`, `PluginEnabledEvent`, …),
temporary plugin file cleanup, full shutdown.

---

## 5. Lifecycle Flow (load path)

```
Host startup
   │
   ▼
DefaultPluginManager.loadAllPlugins()
   │  for each *.jar in pluginsDirectory
   ▼
loadPlugin(jar)
   ├── JarFile → MANIFEST.MF → loadDescriptor()
   ├── new URLClassLoader([jar], appClassLoader)
   ├── loadClass(descriptor.mainClass).newInstance()
   ├── create per-plugin data dir under context.getPluginDataPath()
   ├── build context:
   │     Extended host context? → DefaultExtendedPluginContext (shared ServiceLocator)
   │     else                   → DefaultPluginContext
   ├── plugin.initialize(context)
   ├── if config.isAutoEnable() → plugin.enable() + record state
   └── plugins.put(name, plugin); publish PluginLoadedEvent
```

Enable/disable at runtime goes through `enablePlugin(name)` / `disablePlugin(name)`,
which also (un)registers `MenuProvider` instances and publishes
`PluginMenuAdded/RemovedEvent` + `PluginEnabled/DisabledEvent`.

Shutdown: `shutdownAll()` → `unloadAllPlugins()` (each `plugin.shutdown()`, close
classloader, publish `PluginUnloadedEvent`) → cleanup temp files → `eventBus.shutdown()`.

---

## 6. Event System

Two coexisting buses:

| Bus | Scope | Type |
|---|---|---|
| Core `EventBus` (`ide-core`) | Application-wide (IDE + all plugins) | `com.protonmail.landrevillejf.swingide.core.bus.EventBus` |
| `PluginEventBus` | One instance per plugin context (+ one inside `AbstractPlugin`) | Local pub/sub |

`PluginEventBus` internals: `ConcurrentHashMap<Class<? extends Event>,
CopyOnWriteArrayList<EventListener<?>>>`; dispatch is **synchronous and exact-type**
(no superclass matching), without per-listener error isolation.

Built-in event catalog (`events/`, ~36 classes extending `BaseEvent`):

- Application: `ApplicationStartedEvent`, `ApplicationClosingEvent`, `ApplicationThemeChangedEvent`
- Project: `ProjectCreatedEvent`, `ProjectOpenedEvent`, `ProjectClosedEvent`
- Files: `FileCreatedEvent`, `FileDeletedEvent`, `FileOpenedEvent`, `FileSavedEvent`
- Editor: `EditorCaretMovedEvent`, `EditorSelectionChangedEvent`, `EditorTextChangedEvent`, `EditorEvents.*`
- Build/Run: `BuildStartedEvent`, `BuildFinishedEvent`, `CodeCompiledEvent`, `RunStartedEvent`, `RunFinishedEvent`
- Plugin system: `PluginLoadedEvent`, `PluginUnloadedEvent`, `PluginEnabledEvent`, `PluginDisabledEvent`, `PluginStatusChangedEvent`, `PluginMenuAddedEvent`, `PluginMenuRemovedEvent`
- UI/Tabs: `TabOpenedEvent`, `TabClosedEvent`, `TabSelectedEvent`, `SelectTabEvent`, `UIComponentAddedEvent`, `UIComponentRemovedEvent`, `MenuItemClickedEvent`

---

## 7. Service Layer

All services are keyed by `pluginId` so implementations can quota/scope per plugin.

| Service | Interface | Default impl | Purpose |
|---|---|---|---|
| Logging | `PluginLoggingService` | `DefaultPluginLoggingService` | Per-plugin levels, console/file output, recent-log buffer |
| Cache | `PluginCacheService` | `DefaultPluginCacheService` | TTL entries, eviction policies, max size, stats |
| Notification | `PluginNotificationService` | `DefaultPluginNotificationService` | Typed/prioritized notifications, actions, listeners |
| Metrics | `PluginMetricsService` | `DefaultPluginMetricsService` | Counters, timers, histograms, gauges, export |
| Permission | `PluginPermissionService` | `DefaultPluginPermissionService` | Permissions, roles, audit log |
| Async tasks | `PluginAsyncTaskExecutor` | `DefaultPluginAsyncTaskExecutor` | Priority queues per plugin, scheduled/periodic tasks, cancellation |
| Data store | `PluginDataStore` | `DefaultPluginDataStore` | Persistent KV (JSON/XML via Jackson), backup/restore |
| Config validation | `PluginConfigurationValidator` | `DefaultPluginConfigurationValidator` | Schema registry, custom validators, defaults merge |
| Hooks | `PluginHookService` | `DefaultPluginHookService` | Priority-ordered hooks around IDE operations |
| Resources | `PluginResourceManager` | `DefaultPluginResourceManager` | Cross-plugin resource sharing with access grants |
| Dependencies | `PluginDependencyResolver` | `DefaultPluginDependencyResolver` | Dependency graph, circular detection, resolution order |
| Update | `PluginUpdateService` | `DefaultPluginUpdateService` | Channels, version history, install/rollback |
| Monitoring | `PluginMonitoringService` | `DefaultPluginMonitoringService` | Health reports, alerts, CPU/memory/thread stats |

Resolution chain (`DefaultExtendedPluginContext`): context-level cache →
`PluginServiceLocator` → (fallback) stub implementations from `PluginServiceInitializer`.

### Async executor internals
`DefaultPluginAsyncTaskExecutor` keeps one `PluginExecutor` per plugin:
a `PriorityBlockingQueue` drained by a dedicated worker thread into a fixed thread pool
(`max(2, #cores)`, daemon threads). Task metadata (`PluginTask` states
PENDING/RUNNING/COMPLETED/FAILED/CANCELLED) is tracked in a map cleaned hourly.

---

## 8. UI Integration

- `UIComponent.ComponentType`: `IDE_TAB`, `BOTTOM_PANEL`, `LEFT_SIDEBAR`,
  `RIGHT_SIDEBAR`, `TOOLBAR_BUTTON`, `MENU_ITEM`, `DOCKABLE_PANEL`, `STATUS_BAR_COMPONENT`.
- `UIComponentBuilder` (fluent API) constructs and registers components via
  `ComponentRegistry` — a **static, class-level singleton** shared by all plugins.
- `UIComponentAccessor` is the host-provided facade plugins use to manipulate IDE chrome
  (open files, select tabs, build tasks, dialogs). It is stored as a **static field** on
  `DefaultPluginContext`.
- Plugin types that contribute UI: `MenuProvider` (menu items), `ToolBarProvider`
  (toolbar buttons), `UIComponentProvider` (registered components).

---

## 9. Classloading & Isolation (current state)

- `DefaultPluginManager.loadPlugin()` uses a plain `URLClassLoader(urls, appClassLoader)`.
  Plugins therefore see the entire host classpath — **no isolation today**.
- `SecurePluginClassLoader` (allow-list based) exists but is not wired into the load
  path and its allow-list targets the wrong package. See AUDIT.md §4.1 C-2.
- Unloading closes `URLClassLoader` instances to release JAR file handles.

---

## 10. Persistence & Configuration

- Per-plugin data directory: `<context.getPluginDataPath()>/<plugin-name>/`, created at
  context construction.
- `PluginConfig`: settings map, feature flags, `autoEnable`; `PluginConfig.DEFAULT`
  singleton; persisted via `PluginDataStore` (Jackson JSON/XML serialization).
- Plugin discovery metadata comes from `MANIFEST.MF` (primary) or `plugin.properties`
  (`plugin.class` key, secondary path used by `loadAndEnablePluginByName`).

---

## 11. External Dependencies

| Dependency | Version | Usage | Scope |
|---|---|---|---|
| `ide-core` | 0.3.2-SNAPSHOT (local jar) | `EventBus`, `ServiceRegistry` | implementation |
| `common` | 1.1.1-SNAPSHOT (local jar) | shared utilities | implementation |
| `IconManager` | 1.6.0 (local jar) | icons for UI components | implementation |
| `project-manager` | 1.0-SNAPSHOT (local jar) | `Project` model | implementation |
| `rsyntaxtextarea` | 3.6.0 | code editor component (`CodeEditorExtension`) | implementation |
| Jackson core/databind/annotations/xml | 2.15.2 | config & data store serialization | implementation |
| Lombok | 1.18.30 | boilerplate reduction | implementation* |
| JUnit 5 / Mockito / AssertJ | 5.10.0 / 5.3.1 / 3.24.1 | unit tests | test |
| Cucumber | 7.14.0 | BDD feature tests | test |

\* Audit recommends `compileOnly` — see AUDIT.md §4.4 L-5.

---

## 12. Build & Quality Pipeline

- **Gradle** (`java-library`, `jacoco`, `checkstyle`, `pitest`, `owasp-dependencycheck`,
  `sonarqube`). Artifacts: standard jar + `plugin-api-fat.jar` (wired into `assemble`).
- **Tests**: `test` (JUnit Platform, headless AWT) and `cucumberTest` (BDD glue in
  `bdd.steps`, features in `src/test/resources/features`).
- **Quality gates**: JaCoCo report (excludes `DefaultPluginManager`,
  `SecurePluginClassLoader`, test/bdd classes), PIT mutation (thresholds 0),
  OWASP dependency check (fails on CVSS ≥ 7), Checkstyle, SonarQube.
- **CI** (GitHub Actions): `gradle.yml` (build+test on PR/push), `release.yml`
  (tag `v*` → build, fatJar, GitHub Release), `codeql.yml`, `dependency-review.yml`,
  `gradle-publish.yml`, `update-version.yml`, plus community workflows
  (`greetings`, `stale`, `summary`, `manual`).

---

## 13. Design Principles & Constraints

1. **Fail-isolated loading** — one broken JAR must not prevent other plugins from
   loading (per-JAR try/catch in directory scan).
2. **Local-first services** — plugin-local registrations shadow global registry entries.
3. **Headless-safe tests** — AWT headless mode enforced for CI.
4. **Swing EDT contract** — UI mutations expected on the EDT (`SwingUtilities.invokeLater`
   used in context notifications).
5. **Per-plugin scoping** — every service method takes `pluginId` for quotas, auditing
   and cleanup on unload.

Known deviations from these principles are catalogued in
[AUDIT.md](AUDIT.md) and remediated in [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md).
