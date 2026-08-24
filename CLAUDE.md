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

- `/composeApp` - Main application module containing shared and platform-specific code
- `/iosApp` - iOS application entry point and SwiftUI code
- `/gradle` - Gradle wrapper and configuration files

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

## Technology Stack

- **Language:** Kotlin
- **UI Framework:** Compose Multiplatform
- **Build System:** Gradle with Kotlin DSL
- **Testing Frameworks:** Kotest, JUnit, Kotlin-Test
- **Platforms:** Android, iOS, Desktop (JVM)

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
is JVM-only and `commonTest` also compiles for iOS) and the three suites that open the Couchbase
database (its Android artifact needs a `Context`). When adding a test that touches either area,
expect it to run on the JVM target only.

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

Kover is applied to `composeApp`, `gunzou-here-client` and `password-strength`; the root project
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
pinned to 7.9.1: the 7.7.0 that Compose 1.11.1 defaults to reads class file 68 at most and dies on
a JDK 25 jmod.

```bash
./gradlew :androidApp:assembleRelease :androidApp:bundleRelease   # APK + AAB (currently unsigned)
./gradlew :composeApp:packageReleaseDistributionForCurrentOS      # dmg / msi / deb
```

Keep rules live with the module that needs them:

| File                                                 | Scope                                                                   |
|------------------------------------------------------|-------------------------------------------------------------------------|
| `gunzou/gunzou-here-client/proguard-consumer-rules.pro` | published as Android consumer rules, also included by the desktop build |
| `composeApp/proguard-consumer-rules.pro`             | published as Android consumer rules, also included by the desktop build |
| `composeApp/proguard-desktop-rules.pro`              | desktop only (Couchbase JNI, logback, JNA, MapLibre FFI/LWJGL, enums)   |
| `androidApp/proguard-rules.pro`                      | application-level (`-dontobfuscate`, Parcelize)                         |

### Dependency licenses

The list behind the licenses screen (`ui/settings/LicensesPage.kt`) is generated by the
AboutLibraries Gradle plugin from `:composeApp`'s classpaths, which is what makes it cover the whole
application: project dependencies are not reported as entries, but the graph walk continues into
their own dependencies, so every `gunzou` module is included through `:gunzou-navigator` and
`:gunzou-navigator-client`. The two dependencies `:androidApp` declares for itself are out of scope.

`exportLibraryDefinitions` writes `build/generated/aboutLibrariesResources/files/aboutlibraries.json`
and the file is registered as a Compose resource directory of the platform source sets — a custom
directory *replaces* the one of the source set it is registered on, which is why it cannot go on
`commonMain`. Every build regenerates it; nothing is committed.

A local build never reaches the network: `offlineMode` is on unless
`-Pktravel.licenses.fetchRemote=true` is passed, and it governs the download of the SPDX license
texts as well, so a development build lists licenses by name and leaves their text empty. The
release workflow passes that flag together with `GITHUB_TOKEN`, so only the shipped artifacts carry
the complete data.

The Android side of the plugin also generates a `res/raw/aboutlibraries.json`. Nothing reads it —
the screen reads the Compose resource, which lands in `assets` — so R8's optimized resource
shrinking, on by default since AGP 9, drops it from the release APK. That is why the `res/raw/keep.xml`
the AboutLibraries README recommends is deliberately absent: it is only needed by the Android-only
loading path (`Libs.Builder().withContext(context)`), which this integration does not use.

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

### General Principles

- Write idiomatic Kotlin code
- Follow Kotlin coding conventions
- Use meaningful variable and function names
- Keep functions small and focused on a single responsibility

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
