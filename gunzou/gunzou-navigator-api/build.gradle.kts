import dev.detekt.gradle.Detekt
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
}

// The wire contract of gunzo-navigator, and nothing else: the DTOs the server answers with and the
// ones it accepts. Both sides depend on this module, so it carries no Ktor and no engine specific
// dependency — adding one here would drag it into every consumer, the app included.

group = "com.takaotech.ktravel"
version = "1.0.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

kotlin {
    android {
        namespace = "com.takaotech.navigator.api"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_24)
        }

        // Runs the commonTest suite on the Android JVM as well, so the contract is verified against
        // the Android variant and not only against the desktop one. These tests use kotlin.test,
        // which resolves to JUnit 4 here: no useJUnitPlatform() must be applied, or nothing would
        // be discovered.
        withHostTestBuilder { }.configure {
            isReturnDefaultValues = true
        }

        // Keep rules shipped to consumers that minify (see androidApp). `publish` is required:
        // consumer rules of a KMP library are not published by default.
        optimization {
            consumerKeepRules.publish = true
            consumerKeepRules.file("proguard-consumer-rules.pro")
        }
    }

    // No `binaries.framework` here on purpose: this module is only ever consumed through
    // :gunzou-navigator and :gunzou-navigator-client, which export their own frameworks.
    iosArm64()
    iosSimulatorArm64()

    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_25)
        }
    }

    sourceSets {
        commonMain.dependencies {
            // `api` and not `implementation`: the DTOs are @Serializable, so every consumer needs
            // the runtime on its own compile classpath to encode and decode them.
            api(libs.kotlinx.serialization.json)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
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

    source.setFrom("src/commonMain/kotlin")
}

tasks.withType<Detekt>().configureEach {
    exclude("**/build/**", "**/generated/**")
    reports {
        markdown.required.set(true)
    }
}
