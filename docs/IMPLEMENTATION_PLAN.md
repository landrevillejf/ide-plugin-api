# IDE Plugin API — Implementation Plan

Roadmap turning the [AUDIT.md](AUDIT.md) findings and [OPTIMIZATION.md](OPTIMIZATION.md)
items into concrete releases. Optimizations are referenced as `O-n`.

---

## Release Strategy

| Release | Theme | Target |
|---|---|---|
| **1.3.0-RC2** (hotfix) | Correctness & leaks only — no API changes | ~1 week |
| **1.3.0 GA** | Security isolation + quality gates + docs | ~2–3 weeks |
| **1.4.0** | API ergonomics, build modernization, DX | ~4–6 weeks |
| **2.0.0** | Breaking API cleanup (interface split, ID keying, static removal) | Next cycle |

Gate for every phase: `./gradlew test cucumberTest` green, no new CodeQL/OWASP findings.

---

## Phase 1 — Hotfix release 1.3.0-RC2 (correctness only)

No public API changes. All items are bug fixes; safe to cherry-pick.

| # | Task | Refs | Effort | Acceptance criteria |
|---|---|---|---|---|
| 1.1 | Fix `loadAndEnablePluginByName`: put plugin in registry **before** `enablePlugin`; only instantiate the JAR whose descriptor name matches; never initialize non-matching classes | Audit C-1, O-17 | S | New unit test: loading a directory with 3 jars enables exactly the requested one; no `IllegalArgumentException` logged |
| 1.2 | Fix default `Plugin.setState` semantics: make it abstract-like contract clear — move state storage into `AbstractPlugin.setState` (validate + store), deprecate the interface default | Audit H-6 | S | Test: state actually changes after `setState(ENABLED)` on `AbstractPlugin` |
| 1.3 | Concurrency: `plugins` + `pluginEnabledStates` → `ConcurrentHashMap`; synchronize-free `findPluginByName` via index map | Audit H-1, O-4 | M | jcstress-style test or 100-iteration concurrent load/enable test passes |
| 1.4 | Single source of truth for enabled state (drop `pluginEnabledStates`) | Audit H-2, O-5 | S | `getAllPluginStates()` derived from `PluginStatus`; `unloadPlugin` leaves no stale entry |
| 1.5 | Async executor: shared daemon scheduler; stop creating a scheduler per `schedule()` call; cancel futures on `shutdown(pluginId)` | Audit H-3, O-1/O-2 | M | JVM exits without `System.exit`; thread count constant after 100 scheduled tasks |
| 1.6 | `PluginEventBus`: try/catch per listener (log + continue) | Audit H-4, O-3 | S | Test: throwing listener doesn't prevent later listeners |
| 1.7 | `DefaultPluginContext.unregisterService` also unregisters from global registry; `logInfo` logs at INFO | Audit H-5, M-4 | S | Test asserts global registry empty after unregister |

**Exit criteria**: 1525+ tests green, new regression tests for 1.1–1.7, RC2 tagged.

---

## Phase 2 — 1.3.0 GA (security + quality gates)

| # | Task | Refs | Effort | Acceptance criteria |
|---|---|---|---|---|
| 2.1 | Fix `SecurePluginClassLoader` allow-list (`...ide.plugin`) and wire it into `loadPlugin()` behind a `Plugin-Isolation` manifest attribute (default ON for new plugins) | Audit C-2, O-8 | L | Integration test: hostile plugin accessing `java.io.File` outside its sandbox is blocked; existing sample plugin loads with isolation |
| 2.2 | Permission enforcement at service boundaries (data store cross-plugin access, resource grants) | O-10 | M | Tests: unpermitted plugin gets `SecurityException` / denial |
| 2.3 | Remove JaCoCo/PIT exclusions for `DefaultPluginManager`, `SecurePluginClassLoader`; add their missing tests | Audit §3, O-14 | M | Both classes ≥ 60% branch coverage |
| 2.4 | Enable real thresholds: PIT `mutationThreshold=60`, JaCoCo verification rule (80% instr / 65% branch), `skipFailingTests=false` | O-14 | S | Gates fail when artificially regressed |
| 2.5 | Build fixes: `layout.buildDirectory` everywhere, drop dead javadoc wiring, remove leaked `slf4j.provider` jvmArg | O-12 | S | Clean build without deprecation warnings; no SLF4J `ClassNotFoundException` in test output |
| 2.6 | Detach `fatJar` from `assemble`; update `release.yml` accordingly | O-11/O-15 | S | `./gradlew assemble` no longer builds fat jar; release still publishes it |
| 2.7 | Refresh README badges + compatibility table (1.3.0), link `docs/`, publish changelog | O-21 | S | Badges match last CI run |
| 2.8 | Re-enable javadoc task, fix warnings, attach javadoc jar | O-22 | S | `gradle javadoc` clean |

**Exit criteria**: RC2 soak-tested in the host IDE; 1.3.0 tagged with published javadoc.

---

## Phase 3 — 1.4.0 (ergonomics, modernization, DX)

| # | Task | Refs | Effort |
|---|---|---|---|
| 3.1 | Unified load pipeline (`discover → readDescriptor → validate → load → initialize → enable`) replacing the 3 legacy paths (deprecate old methods) | O-17 | M |
| 3.2 | Event bus v2: hierarchy-aware dispatch + optional async dispatcher | O-3 | M |
| 3.3 | Dependency hygiene: Lombok → `compileOnly`; publish `ide-core`/`common`/etc. to GitHub Packages and consume as versioned deps (drop flatDir) | O-13 | M |
| 3.4 | Jackson bump to current 2.x patch line + OWASP re-run | O-13 | S |
| 3.5 | Gradle: configuration cache + build cache + daemon JVM tuning | O-11 | S |
| 3.6 | Extract stub services to `service.stub`, make thread-safe | O-20 | S |
| 3.7 | Notification service replaces modal `JOptionPane` in `DefaultPluginContext.showNotification` | Audit M-5 | S |
| 3.8 | Plugin SDK docs: sample plugin project template + migration notes | — | M |

**Exit criteria**: host IDE runs on 1.4.0 with isolation ON; build time reduced
(measure baseline before/after).

---

## Phase 4 — 2.0.0 (breaking API cleanup)

| # | Task | Refs | Effort |
|---|---|---|---|
| 4.1 | Split `Plugin` into capability interfaces; core `Plugin` = identity + lifecycle | O-16 | L |
| 4.2 | Re-key plugin registry by **ID**; name-based lookups removed | Audit M-1, O-18 | M |
| 4.3 | `PluginManager.getPluginContext()` → `getPluginContext(String pluginId)` | Audit M-2 | S |
| 4.4 | Remove static `ComponentRegistry`/`UIComponentAccessor`; constructor injection | O-19 | M |
| 4.5 | Remove deprecated methods from 1.x (`loadPlugins`, name-based APIs, legacy stubs) | — | S |
| 4.6 | Migration guide + automated migration checklist in docs | — | M |

**Exit criteria**: zero deprecated API remaining; host IDE migrated; changelog published.

---

## Cross-Cutting Workstreams (continuous)

1. **Testing**: add concurrency tests (manager, event bus, async executor), raise branch
   coverage on transition edges of `PluginStatus`, keep Cucumber features in sync with
   new service behavior.
2. **Docs**: maintain `docs/` (AUDIT, ARCHITECTURE, OPTIMIZATION, this plan), ADRs for
   decisions in 2.1 / 4.1 / 4.2 (O-23), keep README badges truthful (O-21).
3. **Observability**: expose `PluginMonitoringService` health via a host status panel;
   add startup-time metrics per plugin (`getAverageStartupTime` is currently unused).
4. **Security**: quarterly OWASP dependency review; re-run CodeQL on every PR (already
   configured); consider JAR signing (O-9) once a plugin distribution channel exists.

---

## Risks & Mitigations

| Risk | Likelihood | Impact | Mitigation |
|---|---|---|---|
| Classloader isolation (2.1) breaks existing plugins | Medium | High | Opt-out manifest attribute; soak test with all bundled plugins before default-ON |
| Dropping `pluginEnabledStates` (1.4) changes observable behavior | Low | Medium | Derive state from `PluginStatus` behind the same API; regression tests |
| Publishing internal libs (3.3) blocked by infra | Medium | Low | Fallback: keep flatDir but pin checksums |
| Breaking changes in 2.0 fragment the plugin ecosystem | Medium | High | Long deprecation window in 1.4; migration guide; sample plugin CI |

---

## Definition of Done (per item)

- Code merged with unit/integration tests covering the change and the original bug.
- No new Checkstyle/PMD/CodeQL violations; OWASP report unchanged or improved.
- Documentation updated (README / docs / changelog) where behavior changed.
- Verified in the host IDE for anything touching loading, isolation, or UI.
