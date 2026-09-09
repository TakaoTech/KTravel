# KTravel Project Guidelines

# AGENTS.md

## Project Overview

KTravel is a Kotlin Multiplatform travel planning application built with Compose Multiplatform. The
application targets multiple platforms including Android, iOS, and Desktop (JVM).

### Key Features

- Cross-platform travel planning functionality
- Shared UI code using Compose Multiplatform
- Clean architecture with separation of concerns (domain, presentation, UI layers)
- Map integration through MapLibre Compose, called directly from the UI layer

## Project Structure

### Root Level

- `/androidApp` - Android application module (produces the APK/AAB)
- `/composeApp` - Main shared module containing shared and platform-specific code
- `/iosApp` - iOS application entry point and SwiftUI code
- `/gunzou` - The routing stack: wire contract, server, clients (see the table below)
- `/password-strength` - Standalone password strength library
- `/gradle` - Gradle wrapper and configuration files
- `/config/detekt` - Detekt configuration

### Gradle modules and their packages

The Gradle paths are flat even though the `gunzou` modules live in the `gunzou/` directory on disk.

| Gradle path              | Directory                      | Root package                          |
|--------------------------|--------------------------------|---------------------------------------|
| `:androidApp`            | `androidApp/`                  | `com.takaotech.ktravel`               |
| `:composeApp`            | `composeApp/`                  | `com.takaotech.ktravel`               |
| `:gunzou-api`            | `gunzou/gunzou-api/`           | `com.takaotech.gunzou.api`            |
| `:gunzou-client`         | `gunzou/gunzou-client/`        | `com.takaotech.gunzou.client`         |
| `:gunzou-here-client`    | `gunzou/gunzou-here-client/`   | `com.takaotech.gunzou.here`           |
| `:gunzou-server`         | `gunzou/gunzou-server/`        | `com.takaotech.ktravel.gunzou.server` |
| `:gunzou-server-app`     | `gunzou/gunzou-server-app/`    | `com.takaotech.ktravel.gunzou.server` |
| `:password-strength`     | `password-strength/`           | `com.takaotech.password`              |

- `:gunzou-api` is the wire contract shared by the server and its client; it depends on nothing else
  in the repository.
- `:gunzou-here-client` wraps the HERE vendor API. It is an `implementation` dependency of
  `:gunzou-server` and is deliberately kept off every downstream compile classpath, so the vendor
  DTOs never leak past the contract.
- `:gunzou-server` is the Ktor server, built as a KMP library so `:composeApp` can embed it in
  process. `:gunzou-server-app` is the thin JVM module that packages it for deployment.
- `:gunzou-client` is the HTTP client `:composeApp` talks to the server through.

### ComposeApp Module Structure

The composeApp module follows Kotlin Multiplatform conventions:

- **`/src/commonMain/kotlin`** - Shared code for all platforms
    - `com.takaotech.ktravel.domain` - Business logic and domain models
    - `com.takaotech.ktravel.presentation` - ViewModels and presentation logic
    - `com.takaotech.ktravel.ui` - Compose UI components and screens
    - `com.takaotech.ktravel.core` - Core utilities and shared functionality

- **`/src/commonTest/kotlin`** - Shared tests for all platforms

- **Platform-specific source sets:**
    - `/src/androidMain` - Android-specific code
    - `/src/iosMain` - iOS-specific code
    - `/src/jvmMain` - Desktop (JVM) specific code

### Architecture

The project follows **Clean Architecture** principles:

1. **Domain Layer** - Business logic, models, and use cases
2. **Presentation Layer** - ViewModels and UI state management
3. **UI Layer** - Compose UI components and screens

### Logging, diagnostics and telemetry

Application code logs through **`AppLogger`** (`core/logging`) and names no logging library. Behind it
there is one Kermit `Logger`, built in `core/AppLogging.kt` and bound by `AppGraph`, carrying three
writers:

| Writer | Where the line goes |
|---|---|
| `platformLogWriter()` | Logcat on Android, NSLog on iOS, the console on the desktop |
| `LogBufferWriter` | the in-memory buffer the diagnostics screen watches, and the log file |
| `TelemetryLogWriter` | Kotzilla, from `Info` up, and only with the user's consent |

The same logger is what `:gunzou-client` and the embedded `:gunzou-server` are handed, and what the
SLF4J binding in `core/logging/slf4j` writes into — so a Ktor or a Couchbase line lands on the
diagnostics screen next to the application's own. logback is deliberately absent from the application
and excluded in the root build; the standalone `:gunzou-server-app` keeps it. Nothing in the
repository uses log4j, and the same exclusions keep it that way.

The log is written one file per day under `<filesDir>/logs`, kept for three days by default
(`MIN_LOG_RETENTION_DAYS`..`MAX_LOG_RETENTION_DAYS`, changed by the user from the diagnostics screen).

Telemetry is Kotzilla, and it is optional at build time: the SDK is configured by `composeApp/kotzilla.json`,
which is gitignored. Without that file the Gradle plugin is disabled and `src/telemetryNoopMain` is
compiled instead of `src/telemetryKotzillaMain`, so a clone without a key — CI included — builds and
runs identically, minus the sending. Nothing is ever sent before the user answers the question the
introduction ends on (`IntroFlowScreen`), the answer expires after a year (`CONSENT_VALIDITY`), and
revoking it calls `forgetMe()`.

On iOS the Xcode side of Kotzilla is checked in rather than injected: `iosApp.xcodeproj` carries the
`Kotzilla Dsym` build phase, which uploads the symbols, and `iosApp/iosApp/Info.plist` carries
`KotzillaConsentRequired`, which is what holds a pre-main session back until the notice is answered.
The injector that would rewrite both is disabled in `composeApp/build.gradle.kts`
(`tasks.matching { it.name == "setupKotzillaXcode" }`), because with `consentRequired = true` the
plugin adds a second phase of its own, `Kotzilla Info.plist Inject`, and then reads both as dSYM
scripts, warning on every iOS build that it is skipping the injection. Note that the plugin's own
`autoInjectXcodeScript` flag does not stop it: it is read while the plugin is applied, before the
`kotzilla { }` block runs. Re-enabling the task refreshes the dSYM script when the plugin raises
`KOTZILLA_SCRIPT_VERSION` — delete the `Kotzilla Info.plist Inject` phase it adds back.

## Technology Stack

- **Language:** Kotlin
- **UI Framework:** Compose Multiplatform
- **Build System:** Gradle with Kotlin DSL
- **Testing Frameworks:** Kotest, JUnit, Kotlin-Test
- **Platforms:** Android, iOS, Desktop (JVM)

## Skills

Skills hold the detailed procedures this file only summarises. Load the relevant one **before**
starting the work, not after the first failure.

### Project skills

| Skill                              | Use it when                                                                                                       |
|------------------------------------|-------------------------------------------------------------------------------------------------------------------|
| `documenting-with-kdoc`            | Writing or changing any public Kotlin declaration, or adding KDoc to existing code                                  |
| `generating-api-documentation`     | Generating or configuring the Dokka API documentation, or the jobs that publish it to GitHub Pages                   |
| `generating-dependency-licenses`   | Touching the licenses screen, the AboutLibraries setup, or the release workflow that fetches the license texts       |
| `localizing-strings`               | Adding or changing anything in `composeResources/**/strings.xml`, a `stringResource`, a translation, or a language   |
| `migrating-archive-schema`         | Changing an entity behind the `.ktravel` archive (schema version, `TravelPlanJsonMigration`)                        |
| `previewing-composables`           | Writing or changing a `@Preview`, its `PreviewParameterProvider` or its sample data                                 |
| `text-to-lottie`                   | Creating or fixing the Lottie JSON animations played by Skottie                                                     |

### Android / KMP skills (`android-skills` plugin)

Source https://github.com/rcosteira79/android-skills

`android-skills:android-dev` is the baseline: load it for any Android/KMP task in this repository,
then add the specific skill for the area being touched.

| Skill                                   | Use it when                                                                                                     |
|-----------------------------------------|-------------------------------------------------------------------------------------------------------------------|
| `android-skills:android-dev`            | Baseline for every task here (KMP + Compose Multiplatform)                                                          |
| `android-skills:compose`                | Any `ui/` work: composables, state, navigation, recomposition, `commonMain` UI, `expect`/`actual` UI                 |
| `android-skills:kotlin-flows`           | `Flow` / `StateFlow` in presenters and repositories, exposing UI state                                              |
| `android-skills:kotlin-coroutines`      | Dispatchers, scopes, structured concurrency, cancellation                                                           |
| `android-skills:kmp-boundaries`         | `expect`/`actual`, platform services (files, share, permissions), source set layout                                  |
| `android-skills:kmp-ktor`               | `HttpClient` work in `gunzou-here-client` / `gunzou-client`: engines, serialization, `MockEngine` tests               |
| `android-skills:coil-compose`           | Image loading (`AsyncImage`, attachment previews, `LocalPlatformContext`)                                            |
| `android-skills:android-testing`        | Writing or fixing tests, above all Compose UI tests, test clock and animation determinism                            |
| `android-skills:android-debugging`      | Crashes, ANRs, R8/ProGuard stack traces, Logcat, recomposition bugs, Gradle build failures                           |
| `android-skills:android-ux`             | Material 3 review: touch targets, spacing, accessibility, adaptive layouts                                          |
| `android-skills:modularization`         | Deciding where a declaration belongs across `composeApp` / `gunzou-*` / `password-strength`, and its visibility       |
| `android-skills:android-gradle-logic`   | Build logic, version catalog, configuration shared between modules                                                  |
| `android-skills:gradle-build-performance` | Slow builds, configuration cache, KSP, CI build times                                                             |
| `android-skills:android-source-search`  | Reading AOSP or AndroidX source when the public documentation is not enough                                          |
| `android-skills:koin`                   | Only `gunzou-server`, the Ktor server module and the single module wired with Koin                                 |

The remaining skills in the plugin do not apply to this project, because it does not use those
libraries: `android-retrofit` (Ktor instead), the Room half of `android-data-layer` (Couchbase Lite
through Kotbase instead), `paging`, `datastore`, `rxjava-migration`, `pdf-annotations`. Dependency
injection in `composeApp` is Metro, not Hilt or Koin: ignore any Hilt specific guidance a skill
offers.

## Testing Guidelines

### Running Tests

- **All JVM tests:** `./gradlew jvmTest`
- **All tests:** `./gradlew test`
- **Platform-specific tests:**
    - Android: `./gradlew :composeApp:testAndroidHostTest`
    - Desktop: `./gradlew :composeApp:jvmTest`

The `commonTest` suites run on both JVM flavours: `jvmTest` (desktop) and `testAndroidHostTest`
(the Android local unit test compilation, enabled by `withHostTestBuilder` in the module). There is
no `testDebugUnitTest`: these are AGP *KMP library* modules, not classic Android libraries.

A local Android unit test has no real Android runtime behind it, so a few suites are compiled out
of the Android compilation in `composeApp/build.gradle.kts` and verified on the JVM target only:
`ui/**` (Compose UI tests need Robolectric, which Kotest specs cannot opt into because `@RunWith`
is JVM-only and `commonTest` also compiles for iOS), the three suites that open the Couchbase
database (its Android artifact needs a `Context`) and `IntroFlowDataSourceTest` /
`PrivacyPolicyDataSourceTest`, which read the packaged introduction and privacy policy through
Compose Resources — on Android that means the assets, and a local unit test has no `Context` to
reach them. When adding a test that touches any of those areas, expect
it to run on the JVM target only.

### Couchbase Lite native libraries on Linux

`libLiteCore.so` links against ICU 71, a major no Ubuntu LTS ships (22.04 has 70, 24.04 has 74);
ICU exports version suffixed symbols, so pointing a symlink at another major fails on the symbols.
When building on Linux the `fetchCouchbaseIcuLibraries` task unpacks the three libraries from the
archive Couchbase builds against and packages them as application resources, and
`ensureDatabaseNativeLibraries` (`DatabaseNativeLibraries.jvm.kt`) loads them from the classpath
before the first `DatabaseConfiguration`. `System.load` maps each one under its SONAME, which is
what `libLiteCore.so` asks for, so the dynamic linker never searches the system paths.

This is deliberately packaged rather than installed on the machine: the distribution is self
contained, and `jvmTest` on a Linux runner exercises the same path as the shipped application.
Nothing is produced on macOS or Windows hosts, where the loader is a no-op.

### Coverage (Kover)

Kover is applied to `composeApp`, `password-strength` and every `gunzou` module
(`gunzou-api`, `gunzou-client`, `gunzou-here-client`, `gunzou-server`); the root project
aggregates them into a single report. `androidApp` is excluded on purpose — it is a framework entry
point with no test source set.

- **Aggregated HTML report:** `./gradlew koverHtmlReport` → `build/reports/kover/html/index.html`
- **Aggregated XML report (CI):** `./gradlew koverXmlReport` → `build/reports/kover/report.xml`
- **Console summary:** `./gradlew koverLog`
- **Single module:** `./gradlew :composeApp:koverHtmlReport`

Coverage comes from the JVM target, which is where the Kotest suites run. Generated code (Compose
Resources accessors, `ComposableSingletons`, Metro graphs, `@Preview` functions) is filtered out in
the root `kover { reports { filters { ... } } }` block. No verification threshold is enforced: to
add
one, declare a `verify { rule { minBound(...) } }` inside `reports.total`.

### Test Framework Usage

- Use **Kotest** for shared tests as test runners
- Use **JUnit**, or **Kotlin-Test** as test runners for android tests
- Write tests in `/src/commonTest/kotlin` for shared logic
- Platform-specific tests go in respective test source sets

### Testing Best Practices

- Always run tests after making changes to verify correctness
- Ensure tests pass before submitting changes
- Write tests for new features and bug fixes
- Use the Features as the source of truth for expected behavior
- Write test names in english
- Use "Given When Then" pattern
- Load `android-skills:android-testing` before writing or fixing a test suite

## Build Instructions

### Building for Different Platforms

Note that `composeApp` is an AGP *KMP library* and does not produce an APK. The Android application
is `androidApp`.

**Android (debug):**

```bash
./gradlew :androidApp:assembleDebug
```

**Desktop (JVM):**

```bash
./gradlew :composeApp:packageDistributionForCurrentOS
```

### Release Builds

Both platforms shrink code in release. Obfuscation is deliberately off on both: stack traces stay
readable and nothing that resolves a class by name at runtime can break.

The desktop release needs a JDK with `jmods` (ProGuard reads them as `-libraryjars`, and `jlink`
builds the runtime image from them). `composeApp/build.gradle.kts` requests an Amazon Corretto 25
toolchain by vendor and assigns it to `compose.desktop.application.javaHome`, so `JAVA_HOME` never
matters here; the foojay resolver in `settings.gradle.kts` lets Gradle download it. ProGuard is
pinned to 7.9.1: it has to accept class file 69, which 7.7.0 did not — 7.8.0, the version Compose
1.12.0 defaults to, raised the ceiling, and the pin keeps it from moving back.

```bash
./gradlew :androidApp:assembleRelease :androidApp:bundleRelease   # APK + AAB (currently unsigned)
./gradlew :composeApp:packageReleaseDistributionForCurrentOS      # dmg / msi / deb
```

Keep rules live with the module that needs them:

| File                                                    | Scope                                                                   |
|---------------------------------------------------------|-------------------------------------------------------------------------|
| `gunzou/gunzou-api/proguard-consumer-rules.pro`         | published as Android consumer rules, also included by the desktop build |
| `gunzou/gunzou-client/proguard-consumer-rules.pro`      | published as Android consumer rules, also included by the desktop build |
| `gunzou/gunzou-here-client/proguard-consumer-rules.pro` | published as Android consumer rules, also included by the desktop build |
| `gunzou/gunzou-server/proguard-consumer-rules.pro`      | published as Android consumer rules, also included by the desktop build |
| `composeApp/proguard-consumer-rules.pro`                | published as Android consumer rules, also included by the desktop build |
| `composeApp/proguard-desktop-rules.pro`                 | desktop only (Couchbase JNI, the SLF4J binding, JNA, MapLibre FFI/LWJGL, enums) |
| `androidApp/proguard-rules.pro`                         | application-level (`-dontobfuscate`, Parcelize)                         |

The desktop build cannot read Android consumer rules, so the four `gunzou` files above are also
listed explicitly in the `compose.desktop` ProGuard block of `composeApp/build.gradle.kts`. A module
renamed on disk has to be renamed there too, or the shrinker silently loses those keep rules.

The list behind the licenses screen is generated at build time by AboutLibraries: for anything
touching it, or the release workflow that fetches the license texts, use the
`generating-dependency-licenses` skill.

### Running the Application

**Desktop:**

```bash
./gradlew :composeApp:run
```

**iOS:**
Open `/iosApp` directory in Xcode and run from there.

## Code Style Guidelines

### Language Rule (non-negotiable)

**Everything that ends up in this repository is written in English.** This includes function, class
and variable names, comments, KDoc, test names (`Given ... When ... Then ...`), commit messages and
PR descriptions.

Other languages (like Italian) is used in exactly two places, and nowhere else:

1. The conversation with the user.
2. Translation *values* in `composeResources/values-**/strings.xml` (see the `localizing-strings`
   skill). `composeResources/values/strings.xml` stays English-only.
3. The introduction and the privacy policy, in `composeResources/files/intro/intro_flow_<language>.json`
   and `composeResources/files/privacy/privacy_policy_<language>.json`. That content is versioned data
   rather than a label — an answer is given to a *version* of the policy, and each of the two carries
   its own version — and `composeResources/files` takes no language qualifier, so the language is
   resolved by `LocalizedContentReader`, with English (`..._en.json`) as the file that must exist. The
   frame around it — buttons, titles, settings labels — is in `strings.xml` like everything else.

### General Principles

- Write idiomatic Kotlin code
- Follow Kotlin coding conventions
- Use meaningful variable and function names
- Keep functions small and focused on a single responsibility
- Document every public class, function and property with KDoc in the same edit as the code —
  detekt fails the build otherwise; load `documenting-with-kdoc` for the JetBrains and AndroidX
  KDoc conventions this project follows

### Architecture Guidelines

- Maintain clear separation between domain, presentation, and UI layers
- Keep business logic in the domain layer
- Use ViewModels for presentation logic and state management
- UI components should be pure and stateless when possible

### File Organization

- Place shared code in `commonMain`
- Use platform-specific source sets only when necessary
- Group related files by feature/module
- Follow the existing package structure: `domain`, `presentation`, `ui`

### Compose UI Guidelines

- Prefer composable functions for UI components
- Use state hoisting for reusable components
- Follow Compose best practices for performance
- Keep composables focused and composable
- Place new label strings in `strings.xml` and use id for string references
- Use Immutable package instead of standard List
- For anything touching `strings.xml` or translations, use the `localizing-strings` skill
- Every drawing composable gets a `@Preview`: load `previewing-composables` for where it and its
  fixtures go
- Load `android-skills:compose` before non trivial UI work, and `android-skills:android-ux` when
  reviewing a screen against Material 3

## Development Workflow

1. **Before Making Changes:**
    - Understand the existing architecture and code structure
    - Identify which layer (domain/presentation/UI) needs modification
    - Check if changes should be in commonMain or platform-specific code

2. **During Development:**
    - Write code following the established architecture patterns
    - Add or update tests for modified functionality
    - Ensure code compiles for all target platforms

3. **Before Submitting:**
    - Run relevant tests to verify correctness
    - Check that no existing tests are broken
    - Verify the solution addresses the issue requirements
    - Consider edge cases and error handling

## Additional Notes

- The project uses typesafe project accessors (enabled in settings.gradle.kts)
- Dependencies are managed through Gradle version catalogs (gradle/libs.versions.toml)
- Platform-specific implementations should be minimal; prefer shared code when possible
