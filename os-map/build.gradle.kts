import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.detekt)
}

kotlin {
    // Target declarations - add or remove as needed below. These define
    // which platforms this KMP module supports.
    // See: https://kotlinlang.org/docs/multiplatform-discover-project.html#targets
    android {
        namespace = "com.takaotech.os_map"
        minSdk = libs.versions.android.minSdk.get().toInt()
        compileSdk = libs.versions.android.targetSdk.get().toInt()

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }

//        withHostTestBuilder {
//        }
//
//        withDeviceTestBuilder {
//            sourceSetTreeName = "test"
//        }.configure {
//            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
//        }

        androidResources {
            enable = true
        }
    }

    // For iOS targets, this is also where you should
    // configure native binary output. For more information, see:
    // https://kotlinlang.org/docs/multiplatform-build-native-binaries.html#build-xcframeworks

    // A step-by-step guide on how to include this library in an XCode
    // project can be found here:
    // https://developer.android.com/kotlin/multiplatform/migrate
    val xcfName = "os-mapKit"

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        // TODO: Add SPM plugin
        //  https://maplibre.org/maplibre-compose/getting-started/#swift-package-manager
//        iosTarget.swiftPackageConfig {
//            dependency {
//                remotePackageVersion(
//                    url = URI("https://github.com/maplibre/maplibre-gl-native-distribution.git"),
//                    products = { add("MapLibre", exportToKotlin = true) },
//                    packageName = "maplibre-gl-native-distribution",
//                    version = "6.25.1",
//                )
//            }
//        }

        iosTarget.binaries.framework {
            baseName = xcfName
            isStatic = true
        }
    }
    jvm()

//    js {
//        browser()
//        binaries.executable()
//    }
//
//    @OptIn(ExperimentalWasmDsl::class)
//    wasmJs {
//        browser()
//        binaries.executable()
//    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui)
            implementation(libs.compose.resources)
            implementation(libs.compose.preview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.maplibre.compose)
            implementation(libs.platformtools.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)

            implementation(libs.bundles.osm.jvm)
            implementation(libs.maplibre.compose)
            runtimeOnly("org.maplibre.compose:maplibre-native-bindings-jni:0.13.0") {
                capabilities {
                    requireCapability("org.maplibre.compose:maplibre-native-bindings-jni-${detectTarget()}")
                }
            }
        }
    }
}

dependencies {
    detektPlugins(libs.detekt.composerules)
    detektPlugins(libs.detekt.formatting)
}

detekt {
//    buildUponDefaultConfig = true
    ignoreFailures = true
    config.setFrom(file("$rootDir/config/detekt/detekt.yml"))

    arrayOf(
        "androidMain",
        "commonMain",
        "jvmMain",
        "iosMain"
    ).map {
        "src/$it/kotlin"
    }.let {
        source.setFrom(it)
    }
}

tasks.withType<Detekt>().configureEach {
    exclude("**/build/**", "**/generated/**", "org/koin/ksp/generated/**")
    reports {
        md.required.set(true)
        xml.required.set(true)
//        html.outputLocation.set(file("$rootDir/reports/detekt/composeApp.html"))
    }
}

fun detectTarget(): String {
    val hostOs = when (val os = System.getProperty("os.name").lowercase()) {
        "mac os x" -> "macos"
        else -> os.split(" ").first()
    }
    val hostArch = when (val arch = System.getProperty("os.arch").lowercase()) {
        "x86_64" -> "amd64"
        "arm64" -> "aarch64"
        else -> arch
    }
    val renderer = when (hostOs) {
        "macos" -> "metal"
        else -> "opengl"
    }
    return "${hostOs}-${hostArch}-${renderer}"
}
