import io.gitlab.arturbosch.detekt.Detekt

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.detekt)
}

kotlin {
    androidLibrary {
        namespace = "com.takaotech.navigation"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "NavigationClients"
            isStatic = true
        }
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            // Ktor Client
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)

            // Kotlinx Serialization
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kmp.locale)

            // Coroutines
            implementation(libs.kotlinx.coroutines)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        androidMain.dependencies {
            // Ktor Engine for Android
            implementation(libs.ktor.client.okhttp)
        }

        jvmMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation(libs.logback.classic)
        }

        iosMain.dependencies {
            // Ktor Engine for iOS
            implementation(libs.ktor.client.darwin)
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
