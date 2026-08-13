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

            // Ktor server: only the modules that publish every target this library declares.
            implementation(ktorLibs.server.core)
            implementation(ktorLibs.server.cio)
            implementation(ktorLibs.server.auth)
            implementation(ktorLibs.server.cachingHeaders)
            implementation(ktorLibs.server.contentNegotiation)
            implementation(ktorLibs.server.requestValidation)
            implementation(ktorLibs.server.statusPages)
            implementation(ktorLibs.server.routingOpenapi)
            implementation(ktorLibs.serialization.kotlinx.json)

            implementation(libs.kotlinx.serialization.json)

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
        }

        jvmMain.dependencies {
            // JVM only Ktor modules: none of these publishes a native variant.
            implementation(ktorLibs.server.compression)
            implementation(ktorLibs.server.metrics)
            implementation(ktorLibs.server.openapi)
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
