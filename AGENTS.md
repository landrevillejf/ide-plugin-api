# AGENTS.md — AI Agent Guide for ide-plugin-api

Guidance for AI coding agents (and new contributors) working in this repository.
Read this first; the full architecture reference is [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

---

## 1. Project Overview

**IDE Plugin API** is a Java plugin framework for a Swing-based IDE. It provides plugin
lifecycle management, class loading, a service layer (13 services), an event bus, and UI
integration points.

- Base package: `com.protonmail.landrevillejf.ide.plugin`
- Group: `com.protonmail.landrevillejf.ide.plugin.api` · Version: see `gradle.properties`
- License: MIT · Host IDE libs come from local JARs in `libs/` (flatDir).

## 2. Environment

- **Java 21** (OpenJDK 21+). No other JDKs supported.
- **Gradle** via the wrapper only (`./gradlew`). Do not require a system Gradle.
- macOS/Linux; tests run headless (`-Djava.awt.headless=true` is enforced in the build).
- Local JARs in `libs/` (`ide-core`, `common`, `IconManager`, `project-manager`) are
  required for compilation — they are committed to the repo; never delete them.

## 3. Command Reference

| Task | Command |
|---|---|
| Compile | `./gradlew compileJava compileTestJava` |
| Unit tests (JUnit 5) | `./gradlew test` |
| BDD tests (Cucumber) | `./gradlew cucumberTest` |
| Coverage report (JaCoCo) | `./gradlew jacocoTestReport` → `build/reports/jacoco/test/html/` |
| Mutation testing (PIT) | `./gradlew pitest` |
| Dependency vulnerability scan | `./gradlew dependencyCheckAnalyze` |
| All quality gates | `./gradlew runAllChecks` |
| Standard JAR | `./gradlew jar` → `build/libs/ide-plugin-api-<version>.jar` |
| Fat JAR | `./gradlew fatJar` → `build/libs/plugin-api-*-fat.jar` |
| Full build | `./gradlew build` |

Known noise: test runs currently print an SLF4J `ClassNotFoundException`
(`slf4j.provider` jvmArg leaking into Gradle workers). It is harmless; do not "fix"
tests because of it.

## 4. Repository Layout

```
src/main/java/com/protonmail/landrevillejf/ide/plugin/
├── (root)          Core contracts: Plugin, AbstractPlugin, PluginManager,
│                   DefaultPluginManager, PluginContext / ExtendedPluginContext,
│                   PluginDescriptor, PluginConfig, PluginStatus, PluginEventBus,
│                   SecurePluginClassLoader, MenuProvider, ToolBarProvider
├── events/         Event model (~36 event classes extending BaseEvent)
├── service/        13 service interfaces + PluginServiceLocator
├── service/impl/   Default* implementations
├── ui/             UIComponent, UIComponentBuilder, ComponentRegistry, UIComponentAccessor
└── utils/          PanelUtil, PluginServiceUtils

src/test/           Mirror layout; JUnit 5 + Mockito + AssertJ; Cucumber glue in bdd/
src/test/resources/features/   12 Gherkin feature files
docs/               Audit, architecture, optimization, implementation plan (mkdocs site)
libs/               Internal SNAPSHOT JARs (flatDir dependencies)
.github/workflows/  CI, release, security, docs workflows
```

## 5. Coding Conventions

- **Logging**: Lombok `@Slf4j`; use parameterized messages (`log.debug("x {}", x)`).
  The codebase wraps many calls in `if (log.isDebugEnabled())` — match surrounding style.
- **Lombok**: `@Getter/@Setter/@Slf4j` are idiomatic here; avoid adding more heavy
  Lombok features.
- **Collections**: use `ConcurrentHashMap` for anything touched by the plugin manager or
  services; plugin load/enable/disable paths are called from multiple threads.
- **Swing**: UI mutations must go through `SwingUtilities.invokeLater`; never block the EDT.
- **No new static mutable state.** Existing statics (`DefaultPluginContext`'s
  `ComponentRegistry`/`UIComponentAccessor`) are legacy and slated for removal — do not
  add more.
- **Exceptions**: lifecycle operations catch-and-log per plugin (one broken plugin must
  not break others); rethrow only for caller-requested operations like `enablePlugin`.
- **Comments/Javadoc**: public API requires Javadoc (see `Plugin.java` style). Comments
  in this repo are mixed French/English; write new ones in English.
- Checkstyle is enabled (`config/checkstyle/checkstyle.xml`); keep it green.

## 6. Testing Requirements

- Every bug fix needs a regression test; every new feature needs unit tests.
- Stack: JUnit 5 (`@Test`), Mockito 5, AssertJ. Service behaviors additionally get
  Cucumber scenarios in `src/test/resources/features/*.feature` with steps in
  `src/test/java/.../bdd/steps/`.
- Target gates: instruction coverage ≥ 80%, branch ≥ 65%.
- ⚠️ `DefaultPluginManager` and `SecurePluginClassLoader` are currently **excluded**
  from JaCoCo and PIT in `build.gradle`. If you change them, add tests anyway — the
  exclusion is scheduled for removal (see docs/IMPLEMENTATION_PLAN.md, item 2.3).
- Run at minimum `./gradlew test` before committing; `./gradlew test cucumberTest`
  before pushing.

## 7. Known Pitfalls (verified by audit 2026-08 — see docs/AUDIT.md)

Do not rely on these behaviors; they are scheduled fixes:

1. `DefaultPluginManager.loadAndEnablePluginByName()` is broken (enables before the
   plugin is registered). Don't use it; use `loadPlugin(File)`.
2. `SecurePluginClassLoader` allow-list targets the wrong package and is not wired
   into loading; `loadPlugin` uses a plain `URLClassLoader`.
3. `Plugin.setState()` default only validates, it never stores state. State storage
   lives in `AbstractPlugin`.
4. Enabled state is duplicated (`pluginEnabledStates` map + `PluginStatus`);
   prefer `plugin.getState()` / `PluginStatus.isActive()`.
5. `DefaultPluginContext.logInfo()` logs at DEBUG level; `showNotification()` shows a
   modal `JOptionPane`.
6. `PluginEventBus.publish()` is synchronous with no per-listener error isolation.
7. `DefaultPluginAsyncTaskExecutor` leaks non-daemon scheduler threads; call
   `shutdown(pluginId)` in tests to avoid hanging JVMs.

## 8. Build & Release Rules

- Never hardcode the version; it comes from `gradle.properties` (key `version`).
  The `Update Version` workflow (`update-version.yml`) bumps it, tags, and updates
  README/CHANGELOG.
- Releases are driven by tags `v*` → `release.yml` builds JAR + fat JAR and creates
  the GitHub Release. Do not create releases manually.
- `build.gradle` quirks to know: deprecated `buildDir` usages still present, javadoc
  task disabled, `fatJar` wired into `assemble`. Fixes are tracked in
  docs/OPTIMIZATION.md (O-11/O-12) — feel free to implement them, one per commit.
- Dependencies: Jackson/Lombok/RSyntaxTextArea from Maven Central; internal libs from
  `flatDir libs/`. Do not add new external dependencies without updating OWASP scan
  expectations.

## 9. Documentation Duties

- User-facing docs: `README.md` (plugin developer guide) and the mkdocs site source in
  `docs/` (`mkdocs.yml` at repo root). The site is deployed to GitHub Pages by
  `.github/workflows/docs.yml`.
- Engineering docs to keep current: `docs/AUDIT.md`, `docs/ARCHITECTURE.md`,
  `docs/OPTIMIZATION.md`, `docs/IMPLEMENTATION_PLAN.md`, `src/main/resources/changelog.md`.
- When behavior changes, update README compatibility table + changelog.
- When you fix an audit finding, update the corresponding entry in `docs/AUDIT.md`
  and tick the matching item in `docs/IMPLEMENTATION_PLAN.md`.

## 10. Workflow / PR Expectations

- Branch `main` is protected; CI (`gradle.yml`) must pass: compile, build, unit tests,
  cucumber, coverage report upload.
- Security workflows run automatically: CodeQL (push/PR + weekly), dependency-review
  (PRs), OWASP dependency check.
- Keep commits focused; message style: imperative, optionally conventional-commit
  prefixed (`fix:`, `feat:`, `docs:`, `chore:`).
- Never commit build outputs (`build/`, `target/`, `.gradle/`), IDE files, or secrets.
