# IDE Plugin API — Audit Report

**Project**: `com.protonmail.landrevillejf.ide.plugin.api` v1.3.0-RC1
**Date**: 2026-08-26
**Scope**: Full source audit (main + test), build configuration, CI pipelines, quality gates.

---

## 1. Executive Summary

The IDE Plugin API is a mature, well-tested plugin framework (~17k LOC of production code,
1525 automated tests, all passing). It provides a complete lifecycle model, a service
registry, an event bus, and 13 plugin-facing services.

However, the audit identified **2 critical defects**, **6 high-severity issues** (mostly
concurrency, resource leaks, and a broken security classloader), and a set of
medium/low design smells concentrated in `DefaultPluginManager`, the async executor, and
the `Plugin` interface. The measured quality gates are stronger than what the README
reports, meaning documentation has drifted from reality.

| Verdict area | Status |
|---|---|
| Test suite | ✅ Strong (1525 tests, 0 failures) |
| Line coverage | ✅ 86.0% (measured) vs 72% (README badge — stale) |
| Branch coverage | ⚠️ 61.3% — weakest gate |
| Concurrency safety | ❌ Multiple unsynchronized paths in `DefaultPluginManager` |
| Resource hygiene | ❌ Thread leaks in async executor |
| Security / isolation | ❌ `SecurePluginClassLoader` broken and unused |
| API design | ⚠️ God interface, dual sources of truth, name/id confusion |
| Build config | ⚠️ Deprecated APIs, quality gates partially disabled |
| Documentation | ⚠️ Stale badges, no architecture reference |

---

## 2. Methodology

1. Static review of all 54 main source files (16,886 LOC) and 68 test files.
2. Execution of `./gradlew test` + JaCoCo report parsing (local run, 2026-08-26).
3. Review of `build.gradle`, `gradle.properties`, all 9 GitHub Actions workflows.
4. Cross-check of README claims against measured results.

---

## 3. Measured Metrics (local run, 2026-08-26)

| Metric | Measured | README claims | Delta |
|---|---|---|---|
| Unit tests | **1525 passed / 0 failed / 0 skipped** | 1134 total, 3 failed, 36 skipped | Badges stale |
| Instruction coverage | **85.1%** | 72% | +13 pts |
| Line coverage | 86.0% | — | — |
| Branch coverage | **61.3%** | — | Below typical 70% target |
| Method coverage | 87.4% | — | — |
| Cucumber features | 12 feature files, 36/36 (README) | 36/36 | Consistent |

> ⚠️ Coverage blind spot: `DefaultPluginManager` and `SecurePluginClassLoader` are
> **explicitly excluded** from JaCoCo and PIT in `build.gradle` — precisely the two
> classes with the riskiest logic found in this audit.

---

## 4. Findings

### 4.1 Critical

#### C-1 — `loadAndEnablePluginByName()` is functionally broken
**File**: `DefaultPluginManager.java` (lines ~737-797)

- `enablePlugin(plugin.getName())` is called **before** `plugins.put(...)`, so the lookup
  inside `enablePlugin` always fails with `IllegalArgumentException("Plugin not found")`,
  swallowed by the catch block. The method never succeeds for a matching plugin.
- Worse: it **instantiates and `initialize()`s every plugin class found in every JAR** in
  the directory while searching, including non-matching ones, leaving initialized plugin
  instances with closed classloaders (dangling state, leaked side effects).

**Impact**: any host relying on selective plugin loading silently gets nothing loaded.

#### C-2 — `SecurePluginClassLoader` is broken and never used
**File**: `SecurePluginClassLoader.java`, `DefaultPluginManager.loadPlugin()`

- The allow-list references `com.protonmail.landrevillejf.swingide.plugin`, but the actual
  API package is `com.protonmail.landrevillejf.ide.plugin`. Any plugin class touching the
  API would throw `SecurityException("Unallowed for this class")`.
- `DefaultPluginManager.loadPlugin()` uses a plain `URLClassLoader` with the app
  classloader as parent — **no isolation, no permission enforcement**. The permission
  service (`PluginPermissionService`) is decorative with respect to class loading.

**Impact**: untrusted plugin JARs run with full host privileges; the security feature
advertised in documentation does not work.

---

### 4.2 High

#### H-1 — Concurrency defects in `DefaultPluginManager`
- `findPluginByName()` iterates `plugins.values()` **without holding the lock** while
  other methods mutate the map under `synchronized (plugins)` → risk of
  `ConcurrentModificationException` / stale reads.
- `enablePlugin()` calls `plugins.get()` without synchronization.
- `pluginEnabledStates` is a plain `HashMap` written from enable/disable/load paths and
  read concurrently — should be `ConcurrentHashMap`.

#### H-2 — Dual source of truth for plugin enabled-state
Enabled state lives in **both** `pluginEnabledStates` and `plugin.getState()`
(`PluginStatus`). `isPluginEnabled()` prefers the map; `getPluginStatus()` reads the
plugin. The two can diverge (e.g., after `unloadPlugin`, which never removes the
`pluginEnabledStates` entry).

#### H-3 — Thread leaks in `DefaultPluginAsyncTaskExecutor`
- Constructor spawns `Executors.newScheduledThreadPool(1)` with **non-daemon threads**
  that are never shut down → blocks clean JVM shutdown.
- `PluginExecutor.schedule()` / `scheduleAtFixedRate()` create a **new non-daemon
  `ScheduledExecutorService` per call**. Periodic schedulers are never shut down; even
  after `cancelTask()` the scheduler thread survives.
- `setThreadPoolSize()` calls `shutdownNow()` — queued in-flight tasks are silently dropped.
- Each plugin gets 1 dedicated worker thread + a fixed pool of
  `max(2, #cores)` threads → thread proliferation with many plugins.

#### H-4 — `PluginEventBus.publish()` has no fault isolation
A listener throwing an exception aborts delivery to all remaining listeners and propagates
into the publisher's thread. Dispatch is also fully synchronous — a slow listener blocks
the publisher (often the EDT). No async mode, no hierarchy-aware dispatch
(subclass events don't reach superclass subscribers).

#### H-5 — Asymmetric service (un)registration in `DefaultPluginContext`
`registerService(Class, T)` registers into **both** `localServices` and the global
`ServiceRegistry`, but `unregisterService()` only removes from `localServices`. The global
registry keeps stale instances forever → memory leak + cross-plugin pollution.

#### H-6 — `Plugin.setState()` default is a validated no-op
The interface default validates the transition then **never stores the new state** (no
field exists at interface level). Implementations that forget to override it silently keep
the old state while believing the transition succeeded.

---

### 4.3 Medium

| ID | Finding | Location |
|---|---|---|
| M-1 | Plugins keyed by **display name** instead of unique ID (`plugins.put(pluginName, ...)`) → collisions possible; API parameters named `pluginId` actually carry names | `DefaultPluginManager` |
| M-2 | `PluginManager.getPluginContext()` returns one global context, while every plugin actually receives its own — misleading API | `PluginManager` |
| M-3 | Three overlapping load paths (`loadPlugin`, `loadPlugins`, `loadAndEnablePluginByName`) with duplicated JAR/manifest parsing and divergent descriptor semantics (`Plugin-Id` vs `Plugin-Class` vs `plugin.properties`) | `DefaultPluginManager` |
| M-4 | `DefaultPluginContext.logInfo()` logs at **DEBUG** level (semantic mismatch; INFO messages invisible at INFO) | `DefaultPluginContext` |
| M-5 | `showNotification()` implemented as a **modal `JOptionPane`** — blocking UX; should delegate to the notification service | `DefaultPluginContext` |
| M-6 | Hidden global state: `static UIComponentAccessor` (getter/setter) and `static final ComponentRegistry` shared across all contexts — couples plugins, complicates parallel testing | `DefaultPluginContext` |
| M-7 | `PluginServiceInitializer` embeds 13 stub service classes (~330 LOC) with non-thread-safe `HashMap`s — SRP violation; stubs can silently end up in production contexts | `PluginServiceInitializer` |
| M-8 | `Plugin` interface: ~50 methods / 839 lines ("god interface"), overlapping lifecycle hooks (`preInitialize`/`initialize`/`onStart`/`enable`/`beforeEnable`/`afterEnable`…) with undocumented ordering contract | `Plugin` |
| M-9 | `AbstractPlugin` holds its **own private `PluginEventBus`** in addition to the context bus — event routing ambiguity | `AbstractPlugin` |
| M-10 | Mixed French/English comments and user-facing messages ("Fichier JAR sans manifest") | multiple |

---

### 4.4 Low

| ID | Finding |
|---|---|
| L-1 | README badges stale (tests, coverage, mutation); support links point to `yourusername/swing-ide` |
| L-2 | `build.gradle` uses deprecated `buildDir` (removed in Gradle 9); README badge says Gradle 9.3 |
| L-3 | `javadoc` task disabled wholesale; `jacocoTestReport.dependsOn javadoc` is therefore dead wiring |
| L-4 | `-Dslf4j.provider=...SimpleServiceProvider` jvmArg leaks into Gradle worker JVMs → noisy `ClassNotFoundException` on every test run |
| L-5 | Lombok exposed as `implementation` (leaks into consumer classpath) instead of `compileOnly` |
| L-6 | Local `flatDir` SNAPSHOT JARs (`ide-core`, `common`, …) with no checksum/pinning → non-reproducible builds |
| L-7 | `pitest` thresholds set to 0 and `skipFailingTests = true` → mutation gate never fails |
| L-8 | `fatJar` wired into `assemble` → every build pays the fat-jar cost |
| L-9 | Verbose `if (log.isDebugEnabled())` blocks around parameterized slf4j calls (unnecessary) |

---

## 5. Security Assessment

| Control | State |
|---|---|
| Classloader isolation | ❌ Not enforced (plain `URLClassLoader`) |
| JAR signature / integrity verification | ❌ Absent |
| Permission enforcement at runtime | ❌ `PluginPermissionService` not consulted by loader or services |
| Dependency vulnerability scanning | ✅ OWASP Dependency-Check (CVSS ≥ 7 fails build), CodeQL, dependency-review PR workflow |
| Secrets in repo | ✅ None detected |
| Deserialization safety | ⚠️ Jackson used in data store; polymorphic typing should be reviewed |

**Bottom line**: supply-chain tooling is good; *runtime* plugin sandboxing is not
functional. Until C-2 is fixed, treat the plugin directory as trusted input only.

---

## 6. Test Assessment

**Strengths**
- 1525 tests / 0 failures; JUnit 5 + Mockito + AssertJ + Cucumber BDD (12 feature files).
- Mutation testing (PIT) and coverage (JaCoCo) integrated into the build.

**Weaknesses**
- The two highest-risk classes are excluded from coverage **and** mutation testing.
- Branch coverage 61.3% — error/edge paths in lifecycle transitions under-tested.
- No concurrency tests for `DefaultPluginManager` (the discovered race conditions have no failing test).
- Cucumber glue runs as part of the standard `test` task too; double execution cost.

---

## 7. Documentation Assessment

- README is extensive (900+ lines) but partially stale: badges, `EventBus` type names
  (README shows `EventBus` from context, code returns `PluginEventBus`), and the
  compatibility table stops at 1.2.0 while the project is at 1.3.0-RC1.
- No architecture reference, no contribution/dev guide, no Javadoc artifact
  (javadoc task disabled).
- `changelog.md` exists in resources but is not referenced from README.

---

## 8. Recommendations (prioritized)

1. **Now**: Fix C-1 (order of `plugins.put` before `enablePlugin`; only instantiate the
   matching class) and H-1/H-2/H-3 (concurrency + leaks). All are local, low-risk changes.
2. **Short-term**: Wire `SecurePluginClassLoader` into the load path with a corrected
   allow-list, or remove it and drop the isolation claim (C-2).
3. **Short-term**: Add fault isolation + optional async dispatch to `PluginEventBus` (H-4).
4. **Medium-term**: Re-key plugins by ID, unify load paths, split the `Plugin` interface
   into capability interfaces (M-1/M-3/M-8) — target a 2.0 API release.
5. **Continuous**: Remove coverage exclusions for `DefaultPluginManager` /
   `SecurePluginClassLoader`, raise branch coverage to ≥ 70%, set PIT thresholds > 0.

Detailed sequencing, effort estimates and acceptance criteria are in
[IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md); performance details in
[OPTIMIZATION.md](OPTIMIZATION.md).
