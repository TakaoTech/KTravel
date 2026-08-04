# KTravel

[![Detekt](https://github.com/TakaoTech/KTravel/actions/workflows/detekt.yaml/badge.svg?branch=main)](https://github.com/TakaoTech/KTravel/actions/workflows/detekt.yaml)
[![Release](https://github.com/TakaoTech/KTravel/actions/workflows/release.yaml/badge.svg)](https://github.com/TakaoTech/KTravel/actions/workflows/release.yaml)
[![License: Apache 2.0](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.4.0-7F52FF.svg?logo=kotlin)](https://kotlinlang.org)
[![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.11.1-4285F4.svg)](https://github.com/JetBrains/compose-multiplatform)
[![Platforms](https://img.shields.io/badge/Platforms-Android%20%7C%20iOS%20%7C%20Desktop-lightgrey.svg)](#supported-platforms)

A travel planning application built with Kotlin Multiplatform and Compose Multiplatform. One shared
codebase — UI included — runs on Android, iOS and Desktop (JVM).

KTravel lets you organise a trip as a sequence of days and steps: places to visit, transport legs
between them, schedules, notes and attachments. Data is stored locally in an embedded Couchbase Lite
database, so a plan is fully usable offline, and a whole travel plan can be exported to a portable
archive and imported back on another device.

## Features

- **Travel plans** — multiple trips, each with a name, a date range and a day-by-day itinerary.
- **Places and steps** — add points of interest with coordinates, order them inside a day, and
  attach visit schedules with start/end times.
- **Transport legs** — connect two steps and compute a route through a pluggable routing provider:
  a local offline provider, HERE, or Google Maps (transport mode, alternatives, avoid tolls/ferries,
  departure time).
- **Schedule consistency** — the domain layer validates overlapping or impossible schedules while
  you edit a day.
- **Maps** — an interactive map on every platform through the `:os-map` module (MapLibre on
  Android/iOS, Mapsforge on Desktop) plus device location through `:location-clients`.
- **Notes and attachments** — Markdown notes per step and an attachment inventory (images and
  arbitrary files) stored next to the plan.
- **Archive export/import** — a versioned ZIP archive with a manifest, schema migrations, ID
  remapping and conflict resolution on import.
- **Offline-first storage** — embedded Couchbase Lite database, no backend required.
- **Localization** — English base strings with Italian translations
  (`composeResources/values-it`).

## Supported platforms

| Platform | Target                          | Entry point                                               |
|----------|---------------------------------|-----------------------------------------------------------|
| Android  | `minSdk 26`, `compileSdk 37`    | `:androidApp` (the Android application module)            |
| iOS      | `iosArm64`, `iosSimulatorArm64` | `iosApp/` Xcode project, consuming `ComposeApp.framework` |
| Desktop  | JVM (macOS, Windows, Linux)     | `:composeApp` (`com.takaotech.ktravel.MainKt`)            |

Web (JS/Wasm) targets are present but commented out in `composeApp/build.gradle.kts`; they are not
built today.

## Tech stack

| Area            | Choice                                                                 |
|-----------------|------------------------------------------------------------------------|
| Language        | Kotlin 2.4.0 (Multiplatform)                                           |
| UI              | Compose Multiplatform 1.11.1, Material 3, adaptive layouts             |
| Architecture    | Clean Architecture (domain / presentation / ui) + Circuit              |
| DI              | Metro (compiler-plugin DI) with Circuit code generation                |
| Persistence     | Couchbase Lite (Kotbase)                                               |
| Networking      | Ktor client (OkHttp on JVM/Android, Darwin on iOS)                     |
| Serialization   | kotlinx.serialization, kotlinx.datetime, kotlinx.io                    |
| Maps            | MapLibre Compose (Android/iOS), Mapsforge (Desktop)                    |
| Animations      | Compottie (Lottie)                                                     |
| Testing         | Kotest, kotlin-test, JUnit, Turbine-style Circuit tests, MockK/Mokkery |
| Static analysis | Detekt 2.0.0-alpha.5 with Compose rules and formatting                 |
| Build           | Gradle Kotlin DSL, version catalog, typesafe project accessors         |

## Project structure

```
KTravel/
├── androidApp/          Android application (produces the APK/AAB)
├── composeApp/          Shared KMP library: domain, presentation, UI, data
│   └── src/
│       ├── commonMain/  Code shared by every target (+ composeResources)
│       ├── commonTest/  Shared tests
│       ├── androidMain/ jvmMain/ iosMain/  Platform-specific code
│       └── kzipMain/    Shared `actual` zip implementation wired into leaf source sets
├── iosApp/              SwiftUI entry point and Xcode project
├── os-map/              Map abstraction (MapLibre / Mapsforge / Swift interop)
├── location-clients/    Device location abstraction
├── config/detekt/       Detekt configuration
└── .github/workflows/   CI defined as Kotlin scripts (github-workflows-kt)
```

Inside `composeApp`, `commonMain` follows the package layout `core`, `data`, `domain`,
`presentation`, `ui`, `di`.

## Prerequisites

| Tool        | Requirement                                                          |
|-------------|----------------------------------------------------------------------|
| JDK         | 21+ to run Gradle; the build uses a Java 24 toolchain                |
| Android SDK | API 37 installed, with `sdk.dir` set in `local.properties`           |
| Xcode       | Only for iOS — 16+, with Swift Package Manager support (`spmForKmp`) |
| Git         | Required by the JitPack/Maven dependency resolution of some modules  |

Two build steps have stricter JDK needs:

- **Desktop release packaging** requires an **Amazon Corretto 22–24 JDK that ships `jmods`**
  (`jpackage` uses it). It is selected explicitly by vendor in `composeApp/build.gradle.kts`.
- **Detekt** runs in-process inside the Gradle daemon and does not support JVM 23+; run it with
  `JAVA_HOME` pointing at a **JDK 21**.

## Installation

```bash
git clone https://github.com/TakaoTech/KTravel.git
cd KTravel
```

Create `local.properties` in the repository root pointing at your Android SDK (Android Studio does
this for you when you open the project):

```properties
sdk.dir=/path/to/Android/sdk
```

Then let Gradle resolve everything:

```bash
./gradlew build -x test
```

No API keys are required to build. The HERE and Google Maps routing providers read their keys from
the in-app settings screen at runtime; without a key the app falls back to the local provider.

## Build

`composeApp` is an AGP **KMP library** and does not produce an APK — the Android application is
`androidApp`.

**Android (debug):**

```bash
./gradlew :androidApp:assembleDebug          # macOS/Linux
.\gradlew.bat :androidApp:assembleDebug      # Windows
```

**Desktop (JVM) distribution:**

```bash
./gradlew :composeApp:packageDistributionForCurrentOS
```

**iOS framework:** built by Xcode through the Gradle task wired into the project; open
`iosApp/iosApp.xcworkspace` and build from there.

## Run

**Desktop:**

```bash
./gradlew :composeApp:run
```

**Android:** install the debug APK on a connected device or use the IDE run configuration.

```bash
./gradlew :androidApp:installDebug
```

**iOS:** open the `iosApp/` directory (the `.xcworkspace`) in Xcode and run on a simulator or
device.

## Release

Both platforms shrink code in release (R8 on Android, ProGuard on Desktop). Obfuscation is
deliberately **off** on both: stack traces stay readable and nothing that resolves a class by name
at runtime can break.

```bash
./gradlew :androidApp:assembleRelease :androidApp:bundleRelease   # APK + AAB (currently unsigned)
./gradlew :composeApp:packageReleaseDistributionForCurrentOS      # dmg / msi / deb
```

Android release signing is not configured yet: `assembleRelease` produces an unsigned APK. To enable
it, uncomment the `signingConfigs` block and the `signingConfig` line in
`androidApp/build.gradle.kts`, then provide `KTRAVEL_KEYSTORE_PATH`, `KTRAVEL_KEYSTORE_PASSWORD`,
`KTRAVEL_KEY_ALIAS` and `KTRAVEL_KEY_PASSWORD` as environment variables.

Keep shrinking rules next to the module that needs them:

| File                                           | Scope                                                                   |
|------------------------------------------------|-------------------------------------------------------------------------|
| `location-clients/proguard-consumer-rules.pro` | published as Android consumer rules, also included by the desktop build |
| `os-map/proguard-consumer-rules.pro`           | Android consumer rules                                                  |
| `os-map/proguard-desktop-rules.pro`            | desktop only (Mapsforge / kxml2 / SVG Salamander)                       |
| `composeApp/proguard-consumer-rules.pro`       | published as Android consumer rules, also included by the desktop build |
| `composeApp/proguard-desktop-rules.pro`        | desktop only (Couchbase JNI, logback, JNA, enums)                       |
| `androidApp/proguard-rules.pro`                | application-level (`-dontobfuscate`, Parcelize)                         |

## Tests

```bash
./gradlew jvmTest                     # all JVM tests
./gradlew test                        # all test tasks
./gradlew :composeApp:jvmTest         # desktop/shared tests
./gradlew :composeApp:testDebugUnitTest   # Android unit tests
```

Shared tests live in `composeApp/src/commonTest/kotlin` and use Kotest as the runner. Test names are
written in English and follow the `Given ... When ... Then ...` pattern.

## Static analysis

```bash
JAVA_HOME=/path/to/jdk-21 ./gradlew detektAll
```

`detektAll` runs Detekt on every subproject and merges the XML, Markdown and SARIF reports into
`build/reports/detekt/`.

## Continuous integration

Workflows are written as Kotlin scripts with
[github-workflows-kt](https://github.com/typesafegithub/github-workflows-kt) and generate the YAML
next to them:

| Workflow                             | Trigger                       | What it does                                                                                       |
|--------------------------------------|-------------------------------|----------------------------------------------------------------------------------------------------|
| `.github/workflows/detekt.main.kts`  | push to `main` / `dev`        | runs `detektAll`                                                                                   |
| `.github/workflows/release.main.kts` | manual dispatch or a `v*` tag | builds the Android APK/AAB + R8 mapping and the desktop distributions on macOS, Windows and Ubuntu |

After editing a `*.main.kts` file, regenerate the YAML:

```bash
kotlin .github/workflows/release.main.kts
```

## Contributing

- Everything committed to this repository is written in **English** — identifiers, comments, KDoc,
  test names, commit messages and PR descriptions. The only exception is translation *values* in
  `composeResources/values-<lang>/strings.xml`.
- Keep business logic in the domain layer, presentation logic in Circuit presenters/ViewModels, and
  composables stateless where possible.
- New user-facing strings go into `composeResources/values/strings.xml` (English) and are referenced
  with `stringResource`; translations go into the language-qualified folders with identical keys and
  ordering.
- Run the relevant tests and `detektAll` before opening a pull request.

See [CLAUDE.md](CLAUDE.md) for the full project guidelines.

## License

Licensed under the [Apache License, Version 2.0](LICENSE).
