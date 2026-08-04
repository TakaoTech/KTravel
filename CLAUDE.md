# KTravel Project Guidelines

# AGENTS.md

## Project Overview

KTravel is a Kotlin Multiplatform travel planning application built with Compose Multiplatform. The
application targets multiple platforms including Android, iOS, and Desktop (JVM).

### Key Features

- Cross-platform travel planning functionality
- Shared UI code using Compose Multiplatform
- Clean architecture with separation of concerns (domain, presentation, UI layers)
- Map integration via custom os-map module

## Project Structure

### Root Level

- `/composeApp` - Main application module containing shared and platform-specific code
- `/iosApp` - iOS application entry point and SwiftUI code
- `/os-map` - Custom module for map functionality
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

### Coverage (Kover)

Kover is applied to `composeApp`, `location-clients` and `os-map`; the root project aggregates them
into a single report. `androidApp` is excluded on purpose — it is a framework entry point with no
test source set.

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

```bash
./gradlew :androidApp:assembleRelease :androidApp:bundleRelease   # APK + AAB (currently unsigned)
./gradlew :composeApp:packageReleaseDistributionForCurrentOS      # dmg / msi / deb
```

Keep rules live with the module that needs them:

| File                                           | Scope                                                                   |
|------------------------------------------------|-------------------------------------------------------------------------|
| `location-clients/proguard-consumer-rules.pro` | published as Android consumer rules, also included by the desktop build |
| `os-map/proguard-consumer-rules.pro`           | Android consumer rules                                                  |
| `os-map/proguard-desktop-rules.pro`            | desktop only (Mapsforge / kxml2 / SVG Salamander)                       |
| `composeApp/proguard-consumer-rules.pro`       | published as Android consumer rules, also included by the desktop build |
| `composeApp/proguard-desktop-rules.pro`        | desktop only (Couchbase JNI, logback, JNA, enums)                       |
| `androidApp/proguard-rules.pro`                | application-level (`-dontobfuscate`, Parcelize)                         |

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
- The project includes JitPack repository for additional dependencies
- Platform-specific implementations should be minimal; prefer shared code when possible
