# gunzou-navigator

Ktor server, built as a Kotlin Multiplatform library targeting **JVM**, **Android** and **iOS**.
Started from the [Ktor Project Generator](https://start.ktor.io) and reshaped around the split
between what every target can run and what only the JVM can.

The deployable artifact lives in `:gunzou-navigator-app`, a thin JVM module: the Ktor Gradle plugin
disables `buildFatJar` and `runDocker` as soon as it detects the multiplatform plugin
([KTOR-8464](https://youtrack.jetbrains.com/issue/KTOR-8464)), so it is applied there instead.

## Layout

| Source set | Holds |
|---|---|
| `commonMain` | `module()`, routing, the OpenAPI document, resources, serialization, request validation, caching headers, Koin, `startServer()`, `startServerOnFreePort()` |
| `jvmMain` | `jvmModule()`, compression, Swagger UI, Dropwizard metrics, the Kermit to SLF4J bridge |
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
| `server-swagger` | ✅ | ✅ | ❌ |
| `server-call-logging` | ✅ | ✅ | ❌ |

Android has no dedicated `androidJvm` variant anywhere in this table: it consumes the `jvm` one
through the KGP `jvm -> androidJvm` compatibility rule.

`server-openapi` was dropped with the hand written specification it served (see **The OpenAPI
document** below): it renders a second, static copy of the same document through swagger-codegen,
which costs a code generation pass at every startup and writes the result into a `docs/` directory
relative to the working directory — inside the repository, when the server is started from Gradle.

`server-call-logging` was dropped: it is JVM only and no code ever installed it. Its blocker is
`MDC`, which has no Native counterpart. If per-call logging is ever needed on all targets, a ~30 line
plugin built on the `CallSetup` and `ResponseSent` hooks — both public in `commonMain` of
`ktor-server-core` — replaces it without any dependency.

## The OpenAPI document

The specification is generated from the routing tree by `ktor-server-routing-openapi`, not written
by hand. The paths, the methods, the header parameters and the security requirement of each
operation are read off the routes; every schema is inferred from the `kotlinx.serialization`
descriptor of the type the endpoint receives or answers with. What is left — the prose, the tags and
the statuses an endpoint can fail with — lives in `OpenApi.kt` in `commonMain`, attached to the
routes through `Route.describe`.

| Where | What |
|---|---|
| `commonMain/OpenApi.kt` | `info`, `servers`, tags, and one `RouteOperationFunction` per endpoint |
| `commonMain/Routing.kt` | `.describe(HealthOperation)` and friends, one per route |
| `commonMain/Security.kt` | the bearer scheme's description, on the provider it is inferred from |
| `jvmMain/HttpJvm.kt` | `swaggerUI("swagger")`, reading from `OpenApiDocSource.Routing` |

On the JVM that makes `/swagger` the UI and `/swagger/documentation.yaml` the specification, both
built on the first request. `Application.navigatorOpenApiDoc(version)` returns the same document to
anything else that wants it, on every target — `OpenApiDocumentTest` is what uses it today.

This replaces `gunzou-navigator-app/src/main/resources/openapi/documentation.yaml`, which had
already drifted from the contract it described: it declared a `ProviderCatalogResponse` without the
`version` field the type has carried for some time, and nothing in the build could notice. A schema
that comes from the serializer cannot drift, because it is the same descriptor the endpoint encodes
through.

## Logging

Kermit is the application-wide facade, reached through `appLog`. The writer behind it is per target:

- **JVM** — `Slf4jLogWriter` forwards to SLF4J, so `logback.xml` in `:gunzou-navigator-app` keeps
  owning the format. Kermit's own `platformLogWriter()` would write straight to stdout and bypass it.
- **Android** — Logcat, **iOS** — NSLog, both via `platformLogWriter()`.

Ktor's internal logger (`application.log`) is a separate channel: SLF4J on the JVM, `KtorSimpleLogger`
elsewhere. Merging the two would mean implementing `org.slf4j.Logger` by hand, since on the JVM
`io.ktor.util.logging.Logger` is a typealias to it.

## Starting the server

| Target | Entry point |
|---|---|
| JVM | `:gunzou-navigator-app`, `EngineMain` + `application.conf` → `jvmModule()` |
| Android, iOS | `startServerOnFreePort()` → `embeddedServer(CIO) { module() }`, or `startServer(port, wait)` |
| iOS framework | `startGunzoNavigator()`, exported as `GunzoNavigator` |

The JVM path is the odd one out because `application.conf` resolves its modules by name through
reflection, which does not exist on Native.

The embedded entry points bind `EPHEMERAL_PORT` (`0`) by default, so the operating system hands out a
free port: the server runs inside the host application on Android and iOS, where no fixed port can be
reserved. `startServerOnFreePort()` reads the assigned port back from the bound socket and returns it
in a `RunningServer` — `startServer()` only reports it through Ktor's own startup log.
`:gunzou-navigator-app` keeps the fixed `8080` / `$PORT` instead: it is a deployable artifact, and a
random port would make it unreachable.

## Tasks

| Task | Description |
|---|---|
| `./gradlew :gunzou-navigator:assemble` | Builds all three targets, including the iOS frameworks |
| `./gradlew :gunzou-navigator:jvmTest` | Runs `commonTest` on the JVM |
| `./gradlew :gunzou-navigator:testAndroidHostTest` | Runs `commonTest` on the Android JVM |
| `./gradlew :gunzou-navigator:iosSimulatorArm64Test` | Runs `commonTest` on the iOS simulator |
| `./gradlew :gunzou-navigator-app:run` | Runs the server on `http://0.0.0.0:8080` |
| `./gradlew :gunzou-navigator-app:buildFatJar` | Builds the deployable fat JAR |

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
