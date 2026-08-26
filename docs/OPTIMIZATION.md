# IDE Plugin API — Optimization Plan

Companion to [AUDIT.md](AUDIT.md). Each item lists the problem, the change, and the
expected gain. Effort scale: S (< ½ day), M (½–2 days), L (> 2 days).

---

## 1. Runtime Performance & Resource Hygiene

### O-1 · Fix thread leaks in the async executor (M) — HIGH priority
**Problem** (`DefaultPluginAsyncTaskExecutor`):
- Hourly cleanup scheduler: non-daemon, never shut down → blocks JVM exit.
- `schedule()` / `scheduleAtFixedRate()` allocate a **new non-daemon
  ScheduledExecutorService per call**; periodic ones are never shut down.
- Per plugin: 1 worker thread + fixed pool of `max(2, #cores)` → N plugins ≈ N×(cores+1) threads.

**Change**:
1. Share **one** daemon `ScheduledExecutorService` for cleanup and for all
   scheduled/periodic tasks across plugins.
2. Keep only the per-plugin worker+pool, or migrate to a single shared pool with
   per-plugin fairness quotas.
3. On `shutdown(pluginId)` cancel that plugin's scheduled futures.

**Gain**: no leaked threads, JVM exits cleanly, thread count O(1) instead of O(N×cores).

### O-2 · Safe pool resize (S)
`setThreadPoolSize()` currently `shutdownNow()`s the pool and drops queued tasks.
Replace with resize via `setCorePoolSize()`/`setMaximumPoolSize()` on a
`ThreadPoolExecutor` (no task loss).

### O-3 · Event bus fault isolation & async dispatch (M)
`PluginEventBus.publish()`: wrap each listener in try/catch (log + continue); add an
optional executor-backed `publishAsync()` so slow listeners never block the publisher
(especially the EDT). Also deliver subclass events to superclass subscribers
(walk `getSuperclass()`/interfaces when looking up listeners).

### O-4 · Concurrency hardening in `DefaultPluginManager` (M)
- `plugins` → `ConcurrentHashMap` (drop coarse `synchronized (plugins)` blocks).
- `pluginEnabledStates` → `ConcurrentHashMap`.
- Maintain a secondary `id → plugin` index so `findPluginByName` becomes O(1) instead
  of an O(n) scan under no lock.

### O-5 · Single source of truth for enable state (S)
Remove the `pluginEnabledStates` map; derive state from `plugin.getState()`
(`PluginStatus.isActive()`). Fixes divergence bugs (AUDIT H-2) and removes one map
lookup per call.

### O-6 · Log call micro-optimization (S)
Hundreds of `if (log.isDebugEnabled()) { log.debug(...) }` wrappers around already
parameterized slf4j calls. Where the argument expression is cheap, call
`log.debug("... {}", arg)` directly — less code, same cost. Keep guards only where
argument construction is expensive.

### O-7 · Cache service eviction (S)
`DefaultPluginCacheService` should evict lazily on access + run a periodic sweep on the
shared scheduler (O-1) rather than ad-hoc cleanup, to bound memory under TTL-heavy usage.

---

## 2. Security Optimizations

### O-8 · Wire real classloader isolation (L) — HIGH priority
Fix `SecurePluginClassLoader` allow-list to `com.protonmail.landrevillejf.ide.plugin`
(+ `java.`, `javax.swing.`, plus explicitly declared plugin dependencies), use it in
`DefaultPluginManager.loadPlugin()` when `descriptor.requiresIsolation()` (or always,
with an opt-out manifest attribute). Child-first loading for plugin packages to allow
dependency version overrides per plugin.

### O-9 · JAR integrity checks (M)
Optional manifest attribute `Plugin-SHA256` / support for signed JARs; reject unsigned
JARs when a `plugins.requireSignature=true` host setting is enabled.

### O-10 · Enforce permissions at service boundaries (M)
Gate sensitive service calls (`PluginDataStore` cross-plugin access, async execution,
resource grants) through `PluginPermissionService.hasPermission(pluginId, …)` so the
permission model becomes effective instead of advisory.

---

## 3. Build & CI Optimization

### O-11 · Faster default builds (S)
- Detach `fatJar` from `assemble` (build it only in the release workflow / explicit task).
- Enable Gradle **configuration cache** (`org.gradle.configuration-cache=true` in
  `gradle.properties`) and **build cache**; add JVM args for the daemon
  (`org.gradle.jvmargs=-Xmx2g -XX:+UseParallelGC`).

### O-12 · Fix deprecated/broken build wiring (S)
- Replace deprecated `buildDir` usages with `layout.buildDirectory`
  (required before Gradle 9; README badge already claims 9.3).
- Remove dead `jacocoTestReport.dependsOn javadoc` + `cleanJacoco` ordering hacks;
  either re-enable javadoc (fix warnings) or drop the dependency.
- Remove `-Dslf4j.provider=...` from test `jvmArgs` (it leaks into Gradle worker JVMs
  and currently prints a `ClassNotFoundException` on every run); keep the
  `systemProperty` for the test JVM only.

### O-13 · Dependency hygiene (M)
- Lombok → `compileOnly` (+ `annotationProcessor`) so it stops leaking into consumers.
- Replace local `flatDir` SNAPSHOT jars with versioned artifacts from an internal
  Maven repository (GitHub Packages workflow already exists: `gradle-publish.yml`) or
  at minimum commit checksums; SNAPSHOT + flatDir = non-reproducible builds.
- Bump Jackson 2.15.2 → latest 2.17/2.18 patch line (security + perf), run OWASP check.

### O-14 · Honest quality gates (M)
- Remove `DefaultPluginManager` and `SecurePluginClassLoader` from JaCoCo/PIT
  exclusions; add tests until branch coverage on these classes ≥ 60%.
- PIT: set `mutationThreshold = 60`, `coverageThreshold = 70`,
  `skipFailingTests = false` once the suite is green.
- Add a JaCoCo **verification rule** failing the build below 80% instruction /
  65% branch overall.

### O-15 · CI workflow cleanup (S)
`release.yml` runs `build fatJar` (fatJar is already in assemble → double work).
Consolidate; cache Gradle wrapper+distributions (already done via `actions/setup-java`
cache) and reuse the built fat jar artifact instead of rebuilding in dependent jobs.

---

## 4. API & Maintainability Optimization

### O-16 · Split the god interface (L)
Break `Plugin` into capability interfaces; `Plugin` keeps identity + lifecycle only:

```
Plugin (identity + lifecycle)  ←  all plugins
 ├── PluginStateAware (getState/setState/isEnabled)
 ├── ConfigurablePlugin (config/schema/settings)
 ├── EventAwarePlugin (handleEvent/publishEvent/subscriptions)
 ├── ValidatablePlugin (validate/compatibility/host versions)
 ├── MonitoredPlugin (metrics/healthCheck)
 └── SecurePlugin (permissions/isolation)
```

Keeps 1.x binary compatibility via default methods; prepares a clean 2.0.

### O-17 · Unify plugin loading paths (M)
Collapse `loadPlugin` / `loadPlugins` / `loadAndEnablePluginByName` into one pipeline:
`discover(jar) → readDescriptor (manifest, fallback plugin.properties) → validate →
load → initialize → (optional) enable`. Eliminates ~120 duplicated lines and the C-1 bug.

### O-18 · Key plugins by ID (M)
Internally re-key `plugins` by `descriptor.getId()`; keep name-based lookups as a
deprecated convenience. Requires a migration note in the changelog.

### O-19 · Remove static globals (M)
Move `ComponentRegistry` and `UIComponentAccessor` from static fields on
`DefaultPluginContext` to constructor-injected instances (provide a legacy static
accessor marked `@Deprecated` for one release). Unlocks parallel test execution and
multi-window support later.

### O-20 · Extract stub services (S)
Move `StubPluginServiceLocator` + 13 stub classes out of `PluginServiceInitializer` into
a `service.stub` package with thread-safe collections, or generate them.

---

## 5. Documentation & DX Optimization

### O-21 · Truthful badges (S)
Regenerate README badges from the last CI run (currently stale: 1134/3 failed vs actual
1525/0; 72% vs 85.1% measured). Consider a small `summary.yml`-driven badge update job.

### O-22 · Publish Javadoc (S)
Re-enable the `javadoc` task (fix warnings), publish to GitHub Pages on release;
`withJavadocJar()` is already configured but the task is disabled.

### O-23 · Architecture Decision Records (S)
Adopt lightweight ADRs under `docs/adr/` for the big upcoming decisions
(classloader strategy, interface split, state source-of-truth).

---

## 6. Expected Impact Summary

| Area | Before | After |
|---|---|---|
| Leaked threads per IDE session | 1 + per scheduled task + per plugin | 0 (shared daemon scheduler) |
| Plugin load lookup | O(n) scan, unsynchronized | O(1), lock-free |
| Default build time | fatJar always built | ~30–40% faster `assemble` (est.) |
| Branch coverage gate | none, 61.3% measured | enforced ≥ 65%, risky classes included |
| Plugin sandboxing | none | classloader isolation + permission enforcement |
| Reproducibility | SNAPSHOT flatDir jars | pinned/published artifacts |

Sequencing and acceptance criteria for every item: see
[IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md).
