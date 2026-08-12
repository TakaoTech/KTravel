# gunzo-navigator

Ktor server, built as a Kotlin Multiplatform library targeting **JVM**, **Android** and **iOS**.
Started from the [Ktor Project Generator](https://start.ktor.io) and reshaped around the split
between what every target can run and what only the JVM can.

The deployable artifact lives in `:gunzo-navigator-app`, a thin JVM module: the Ktor Gradle plugin
disables `buildFatJar` and `runDocker` as soon as it detects the multiplatform plugin
([KTOR-8464](https://youtrack.jetbrains.com/issue/KTOR-8464)), so it is applied there instead.

## Layout

| Source set | Holds |
|---|---|
| `commonMain` | `module()`, routing, resources, serialization, request validation, caching headers, Koin, `startServer()` |
| `jvmMain` | `jvmModule()`, compression, OpenAPI, Swagger UI, Dropwizard metrics, the Kermit to SLF4J bridge |
| `androidMain`, `iosMain` | the `appLogWriter()` actual only |
| `commonTest` | `ServerTest`, run on all three targets |

## Which Ktor modules go where

Only the modules that publish a variant for every declared target can sit in `commonMain`. The five
below are JVM only, and are therefore installed by `jvmModule()` rather than by `module()`:

| Ktor module | JVM | Android | iOS |
|---|---|---|---|
| `server-core`, `server-cio`, `server-auth`, `server-caching-headers` | ✅ | ✅ | ✅ |
| `server-content-negotiation`, `server-request-validation` | ✅ | ✅ | ✅ |
| `server-resources`, `server-routing-openapi`, `serialization-kotlinx-json` | ✅ | ✅ | ✅ |
| `server-compression` | ✅ | ✅ | ❌ |
| `server-metrics` | ✅ | ✅ | ❌ |
| `server-openapi` | ✅ | ✅ | ❌ |
| `server-swagger` | ✅ | ✅ | ❌ |
| `server-call-logging` | ✅ | ✅ | ❌ |

Android has no dedicated `androidJvm` variant anywhere in this table: it consumes the `jvm` one
through the KGP `jvm -> androidJvm` compatibility rule.

`server-call-logging` was dropped: it is JVM only and no code ever installed it. Its blocker is
`MDC`, which has no Native counterpart. If per-call logging is ever needed on all targets, a ~30 line
plugin built on the `CallSetup` and `ResponseSent` hooks — both public in `commonMain` of
`ktor-server-core` — replaces it without any dependency.

## Logging

Kermit is the application-wide facade, reached through `appLog`. The writer behind it is per target:

- **JVM** — `Slf4jLogWriter` forwards to SLF4J, so `logback.xml` in `:gunzo-navigator-app` keeps
  owning the format. Kermit's own `platformLogWriter()` would write straight to stdout and bypass it.
- **Android** — Logcat, **iOS** — NSLog, both via `platformLogWriter()`.

Ktor's internal logger (`application.log`) is a separate channel: SLF4J on the JVM, `KtorSimpleLogger`
elsewhere. Merging the two would mean implementing `org.slf4j.Logger` by hand, since on the JVM
`io.ktor.util.logging.Logger` is a typealias to it.

## Starting the server

| Target | Entry point |
|---|---|
| JVM | `:gunzo-navigator-app`, `EngineMain` + `application.conf` → `jvmModule()` |
| Android, iOS | `startServer(port)` → `embeddedServer(CIO) { module() }` |
| iOS framework | `startGunzoNavigator(port)`, exported as `GunzoNavigator` |

The JVM path is the odd one out because `application.conf` resolves its modules by name through
reflection, which does not exist on Native.

## Tasks

| Task | Description |
|---|---|
| `./gradlew :gunzo-navigator:assemble` | Builds all three targets, including the iOS frameworks |
| `./gradlew :gunzo-navigator:jvmTest` | Runs `commonTest` on the JVM |
| `./gradlew :gunzo-navigator:testAndroidHostTest` | Runs `commonTest` on the Android JVM |
| `./gradlew :gunzo-navigator:iosSimulatorArm64Test` | Runs `commonTest` on the iOS simulator |
| `./gradlew :gunzo-navigator-app:run` | Runs the server on `http://0.0.0.0:8080` |
| `./gradlew :gunzo-navigator-app:buildFatJar` | Builds the deployable fat JAR |

A successful start looks like this:

```
2026-08-12 11:13:09.586 [main] INFO  Application - Application started in 0.525 seconds.
2026-08-12 11:13:09.599 [DefaultDispatcher-worker-1] INFO  Application - Responding at http://0.0.0.0:8080
```

## Notes

- **Android**: the consuming app must declare the `INTERNET` permission itself; a library manifest
  cannot guarantee it.
- **iOS**: CIO can bind a local port while the app is in the foreground. iOS suspends open sockets
  once it leaves it.
