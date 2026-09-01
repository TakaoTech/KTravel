import dev.detekt.gradle.Detekt
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
}

// The other side of the contract: what talks to gunzo-navigator, whether it is running in this
// process or on a machine somewhere. Nothing here knows a provider exists — that is the point.

group = "com.takaotech.ktravel"
version = "1.0.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

kotlin {
    android {
        namespace = "com.takaotech.gunzou.client"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_24)
        }

        withHostTestBuilder { }.configure {
            isReturnDefaultValues = true
        }

        optimization {
            consumerKeepRules.publish = true
            consumerKeepRules.file("proguard-consumer-rules.pro")
        }
    }

    // No `binaries.framework`: this module is consumed through :composeApp, which exports its own.
    iosArm64()
    iosSimulatorArm64()

    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_25)
        }
    }

    sourceSets {
        commonMain.dependencies {
            // `api`: every method signature is made of contract types, so a consumer cannot call
            // this client without them.
            api(projects.gunzouApi)

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)
            implementation(libs.kotlinx.coroutines)
            api(libs.kermit)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }

        jvmMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

dependencies {
    detektPlugins(libs.detekt.formatting)
}

detekt {
    ignoreFailures = true
    // No Compose in this module, so the detekt Compose plugin is not on the classpath and the
    // `Compose` section of the shared config is skipped rather than enforced.
    config.setFrom(
        file("$rootDir/config/detekt/detekt.yml"),
        file("$rootDir/config/detekt/detekt-no-compose.yml"),
    )

    arrayOf("androidMain", "commonMain", "iosMain", "jvmMain")
        .map { "src/$it/kotlin" }
        .let { source.setFrom(it) }
}

tasks.withType<Detekt>().configureEach {
    exclude("**/build/**", "**/generated/**")
    reports {
        markdown.required.set(true)
    }
}
