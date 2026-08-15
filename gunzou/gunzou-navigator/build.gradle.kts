import dev.detekt.gradle.Detekt
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
}

// The Ktor Gradle plugin lives in :gunzou-navigator-app instead: it disables buildFatJar and
// runDocker as soon as it detects the multiplatform plugin (KTOR-8464), so the deployable artifact
// is produced by a thin JVM module that depends on this library.

group = "com.takaotech.ktravel"
version = "1.0.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

kotlin {
    android {
        namespace = "com.takaotech.ktravel.server"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_24)
        }

        // Runs the commonTest suite on the Android JVM as well, so the shared server logic is
        // verified against the Android variant and not only against the desktop one.
        withHostTestBuilder { }.configure {
            isReturnDefaultValues = true
        }

        // Keep rules shipped to consumers that minify (see androidApp). `publish` is required:
        // consumer rules of a KMP library are not published by default. The application runs this
        // server in its own process, so the shrinker has to be told what Ktor and Koin resolve by
        // name — a server that does not start does so only in a release build.
        optimization {
            consumerKeepRules.publish = true
            consumerKeepRules.file("proguard-consumer-rules.pro")
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "GunzoNavigator"
            isStatic = true
        }
    }

    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_25)
        }
    }

    sourceSets {
        commonMain.dependencies {
            // The wire contract. `api` and not `implementation`: every endpoint signature is made of
            // these types, so :gunzou-navigator-app and the tests need them on their own classpath.
            api(projects.gunzouNavigatorApi)

            // The vendor client. `implementation` and never `api`: hiding it behind the contract is
            // the whole reason this server exists, and exposing it here would put the HERE DTOs back
            // on the classpath of everything downstream.
            implementation(projects.gunzouHereClient)

            // Ktor server: only the modules that publish every target this library declares.
            implementation(ktorLibs.server.core)
            implementation(ktorLibs.server.cio)
            implementation(ktorLibs.server.auth)
            implementation(ktorLibs.server.cachingHeaders)
            implementation(ktorLibs.server.contentNegotiation)
            implementation(ktorLibs.server.requestValidation)
            implementation(ktorLibs.server.statusPages)
            implementation(ktorLibs.server.rateLimit)
            implementation(ktorLibs.server.routingOpenapi)
            implementation(ktorLibs.serialization.kotlinx.json)

            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines)
            implementation(libs.kotlinx.datetime)

            // Required to build the vendor requests: HereClient types its language parameters as
            // com.vanniktech.locale.Locale, and :gunzou-here-client keeps the dependency internal.
            implementation(libs.kmp.locale)

            // Dependency injection
            implementation(libs.koin.ktor)

            // Logging
            implementation(libs.kermit)
            implementation(libs.kermit.koin)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(ktorLibs.server.testHost)

            // Stands in for HERE: the endpoints are exercised against recorded payloads, so the
            // suite needs no key, no network and costs nothing per run.
            implementation(libs.ktor.client.mock)

            // The other side of the contract, so the round trip can be tested over a real socket.
            // Test only, and only in this direction: the server knows nothing about the client.
            implementation(projects.gunzouNavigatorClient)
        }

        jvmMain.dependencies {
            // JVM only Ktor modules: none of these publishes a native variant.
            implementation(ktorLibs.server.compression)
            implementation(ktorLibs.server.metrics)

            // Swagger UI only. `server-openapi` renders a second copy of the same document through
            // swagger-codegen and writes it to disk at startup; the document itself now comes from
            // the routing tree via `server-routing-openapi`, which is in commonMain.
            implementation(ktorLibs.server.swagger)

            // Kermit output is routed here; the binding is supplied by the consumer.
            implementation(libs.slf4j.api)
        }

        jvmTest.dependencies {
            // Without an SLF4J binding the JVM tests only print the "no providers were found" notice.
            implementation(libs.logback.classic.server)
        }
    }
}

dependencies {
    detektPlugins(libs.detekt.formatting)
}

detekt {
    ignoreFailures = true
    // No Compose in this server module, so the detekt Compose plugin is not on the classpath and the
    // `Compose` section of the shared config is skipped rather than enforced.
    config.setFrom(
        file("$rootDir/config/detekt/detekt.yml"),
        file("$rootDir/config/detekt/detekt-no-compose.yml"),
    )

    arrayOf(
        "androidMain",
        "commonMain",
        "iosMain",
        "jvmMain"
    ).map {
        "src/$it/kotlin"
    }.let {
        source.setFrom(it)
    }
}

tasks.withType<Detekt>().configureEach {
    exclude("**/build/**", "**/generated/**")
    reports {
        markdown.required.set(true)
    }
}
