# Development Guide

Everything needed to build, test and contribute to the IDE Plugin API.
AI coding agents should also read
[`AGENTS.md`](https://github.com/landrevillejf/ide-plugin-api/blob/main/AGENTS.md)
at the repository root.

## Prerequisites

| Requirement | Version | Notes |
|---|---|---|
| JDK | 21+ | OpenJDK 21 tested |
| Gradle | via wrapper (`./gradlew`) | No system Gradle needed |
| OS | macOS / Linux / Windows | Tests run with `-Djava.awt.headless=true` |

The internal libraries (`ide-core`, `common`, `IconManager`, `project-manager`) are
committed as JARs under `libs/` and resolved through a `flatDir` repository.

## Build commands

```bash
./gradlew compileJava compileTestJava   # compile only
./gradlew build                         # full build (includes fatJar via assemble)
./gradlew jar                           # standard jar → build/libs/
./gradlew fatJar                        # fat jar  → build/libs/plugin-api-*-fat.jar
```

## Testing

```bash
./gradlew test           # JUnit 5 unit tests (1500+)
./gradlew cucumberTest   # Cucumber BDD scenarios (src/test/resources/features)
```

Test stack: **JUnit Jupiter 5.10 · Mockito 5.3 · AssertJ 3.24 · Cucumber 7.14**.
Reports: `build/reports/tests/test/` and `build/reports/cucumber/`.

## Quality gates

```bash
./gradlew jacocoTestReport        # coverage → build/reports/jacoco/test/html/
./gradlew pitest                # mutation testing → build/reports/pitest/
./gradlew dependencyCheckAnalyze  # OWASP vulnerabilities (fails on CVSS ≥ 7)
./gradlew checkstyleMain        # style rules (config/checkstyle/checkstyle.xml)
./gradlew runAllChecks          # tests + coverage + mutation + security
```

Coverage targets: **≥ 80% instruction, ≥ 65% branch**.

!!! warning "Coverage blind spot"
    `DefaultPluginManager` and `SecurePluginClassLoader` are currently excluded from
    JaCoCo/PIT in `build.gradle`. Removing that exclusion is tracked as plan item 2.3 —
    if you touch those classes, write tests regardless.

## Package layout

```
com.protonmail.landrevillejf.ide.plugin
├── (root)        Core contracts & lifecycle (Plugin, PluginManager, contexts, …)
├── events        Event model (~36 event classes)
├── service       13 service interfaces
├── service.impl  Default service implementations
├── ui            UI component model & registry
└── utils         Helpers
```

## Conventions

1. **Logging** — Lombok `@Slf4j`, parameterized messages.
2. **Thread safety** — the plugin manager and services are used from multiple threads;
   prefer `ConcurrentHashMap`, avoid coarse locks.
3. **Swing** — mutate UI only via `SwingUtilities.invokeLater`; never block the EDT.
4. **No new static mutable state** — existing statics in `DefaultPluginContext` are
   legacy and scheduled for removal.
5. **Isolation** — one broken plugin must never break others: catch-and-log in
   lifecycle loops; rethrow only for explicit caller operations.
6. **Tests with changes** — every fix ships with a regression test; service behaviors
   also get Cucumber scenarios.
7. **Docs with behavior changes** — update README, `docs/` and
   `src/main/resources/changelog.md` together with the code.

## Release process

1. Run the **Update Version** workflow (`update-version.yml`) with the new version —
   it bumps `gradle.properties`, updates README/CHANGELOG and pushes tag `vX.Y.Z`.
2. The tag triggers `release.yml`: builds the JAR + fat JAR and publishes the
   GitHub Release.
3. `gradle-publish.yml` handles artifact publication.

## CI overview

| Workflow | Trigger | Purpose |
|---|---|---|
| `gradle.yml` | push / PR | Compile, build, unit + cucumber tests, coverage artifact |
| `release.yml` | tag `v*` / manual | Build artifacts, create GitHub Release |
| `gradle-publish.yml` | release / manual | Publish artifacts |
| `codeql.yml` | push / PR / weekly | CodeQL static analysis |
| `dependency-review.yml` | PR | Dependency diff review |
| `docs.yml` | push / PR (docs paths) | Build mkdocs; deploy to GitHub Pages from `main` |
| `update-version.yml` | manual | Version bump + tag |
| `greetings.yml` / `stale.yml` / `summary.yml` | issues / PRs | Community automation |

## This documentation site

The site is built with [MkDocs](https://www.mkdocs.org/) +
[Material theme](https://squidfunk.github.io/mkdocs-material/).

```bash
python3 -m venv .venv && source .venv/bin/activate
pip install -r docs/requirements.txt
mkdocs serve          # live preview at http://127.0.0.1:8000
mkdocs build --strict # production build → site/
```

Sources live in `docs/` with navigation defined in `mkdocs.yml` (repo root).
Deployment to GitHub Pages is automated by `.github/workflows/docs.yml`.
