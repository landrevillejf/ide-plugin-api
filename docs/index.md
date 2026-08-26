# IDE Plugin API

A comprehensive, modular plugin system for the IDE that allows developers to extend the
IDE with custom functionality, UI components, and services.

**Version**: 1.3.0-RC1 · **Language**: Java 21 · **Build**: Gradle · **License**: MIT

---

## What this site covers

This is the **engineering documentation** for the framework itself. For the
plugin-developer guide (how to *write* a plugin), see the
[repository README](https://github.com/landrevillejf/ide-plugin-api#readme).

| Section | Description |
|---|---|
| [Architecture](ARCHITECTURE.md) | Components, lifecycle state machine, event system, service layer, UI integration, build pipeline |
| [Development Guide](DEVELOPMENT.md) | How to build, test and contribute — commands, conventions, quality gates |
| [Audit Report](AUDIT.md) | Findings from the full project audit (correctness, security, testing, build) |
| [Optimization Plan](OPTIMIZATION.md) | Runtime, security, build/CI and API improvements with effort estimates |
| [Implementation Plan](IMPLEMENTATION_PLAN.md) | Phased roadmap from 1.3.0-RC2 to 2.0.0 with acceptance criteria |

## At a glance

```
┌──────────────────────────────────────────────────────────────┐
│                          Host IDE                            │
├──────────────────────────────────────────────────────────────┤
│                     DefaultPluginManager                     │
│   discover JARs → classload → initialize → enable/disable    │
├───────────────────────────────┬──────────────────────────────┤
│   PluginContext (per plugin)  │   PluginEventBus + core bus  │
├───────────────────────────────┴──────────────────────────────┤
│  13 services: Logging · Cache · Notification · Metrics ·     │
│  Permission · Async · DataStore · Hooks · Resources ·        │
│  Dependencies · Update · Monitoring · Config Validation      │
└──────────────────────────────────────────────────────────────┘
```

## Key capabilities

- **Plugin lifecycle** — load → initialize → enable → disable → unload, with a formal
  `PluginStatus` state machine and auto-enable support.
- **Service-oriented** — plugins obtain (and register) services through the context;
  every service is scoped by `pluginId`.
- **Event-driven** — publish/subscribe over a per-plugin bus bridged to the
  application-wide core `EventBus`.
- **UI integration** — tabs, sidebars, toolbars, menus, status bar components via
  `UIComponentBuilder` and `ComponentRegistry`.
- **Configuration & persistence** — `PluginConfig` settings plus a Jackson-backed
  `PluginDataStore` with backup/restore.
- **Quality pipeline** — JUnit 5 + Cucumber, JaCoCo coverage, PIT mutation testing,
  OWASP Dependency-Check, CodeQL, Checkstyle.

## Current status

Latest audit (2026-08-26): **1525 tests passing**, **85.1% instruction coverage**.
Two critical defects identified (broken selective-loading path, non-functional classloader
isolation) — remediation tracked in the [Implementation Plan](IMPLEMENTATION_PLAN.md).

## Repository

- Source & issues: <https://github.com/landrevillejf/ide-plugin-api>
- Releases: <https://github.com/landrevillejf/ide-plugin-api/releases>
- Agent/contributor guide: [`AGENTS.md`](https://github.com/landrevillejf/ide-plugin-api/blob/main/AGENTS.md)
