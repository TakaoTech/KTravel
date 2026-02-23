# KTravel Project Guidelines

## Project Overview

KTravel is a Kotlin Multiplatform travel planning application built with Compose Multiplatform. The application targets
multiple platforms including Android, iOS, Web (WebAssembly and JavaScript), and Desktop (JVM).

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
    - `/src/wasmJsMain` - WebAssembly specific code
    - `/src/jsMain` - JavaScript specific code
    - `/src/webMain` - Common web code

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
- **Platforms:** Android, iOS, Desktop (JVM), Web (Wasm/JS)

## Testing Guidelines

### Running Tests

- **All JVM tests:** `./gradlew jvmTest`
- **All tests:** `./gradlew test`
- **Platform-specific tests:**
    - Android: `./gradlew :composeApp:testDebugUnitTest`
    - Desktop: `./gradlew :composeApp:jvmTest`
    - Web (Wasm): `./gradlew :composeApp:wasmJsTest`

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

**Android:**

```bash
./gradlew :composeApp:assembleDebug
```

**Desktop (JVM):**

```bash
./gradlew :composeApp:packageDistributionForCurrentOS
```

**Web (WebAssembly):**

```bash
./gradlew :composeApp:wasmJsBrowserDistribution
```

**Web (JavaScript):**

```bash
./gradlew :composeApp:jsBrowserDistribution
```

### Running the Application

**Desktop:**

```bash
./gradlew :composeApp:run
```

**Web (Wasm):**

```bash
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```

**Web (JS):**

```bash
./gradlew :composeApp:jsBrowserDevelopmentRun
```

**iOS:**
Open `/iosApp` directory in Xcode and run from there.

## Code Style Guidelines

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
