import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    id("kotlin-parcelize")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(24)
    }
}

android {
    namespace = "com.takaotech.ktravel"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = libs.versions.ktravel.code.get().toInt()
        versionName = libs.versions.ktravel.version.get()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    // Release signing is currently disabled: `assembleRelease` produces an unsigned APK.
    // To enable it, uncomment this block and the `signingConfig` line in the release build type,
    // then export the CI secrets as environment variables:
    //   KTRAVEL_KEYSTORE_PATH      path to the .jks (e.g. written from a base64 secret in a prior step)
    //   KTRAVEL_KEYSTORE_PASSWORD
    //   KTRAVEL_KEY_ALIAS
    //   KTRAVEL_KEY_PASSWORD
    // Environment variables read at configuration time are tracked by the configuration cache, so
    // no extra opt-out is needed. Keystore files are already ignored by .gitignore.
    // signingConfigs {
    //     create("release") {
    //         storeFile = System.getenv("KTRAVEL_KEYSTORE_PATH")?.let(::file)
    //         storePassword = System.getenv("KTRAVEL_KEYSTORE_PASSWORD")
    //         keyAlias = System.getenv("KTRAVEL_KEY_ALIAS")
    //         keyPassword = System.getenv("KTRAVEL_KEY_PASSWORD")
    //     }
    // }
    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            // signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    lint {
        abortOnError = false
    }
}

kotlin {
    target {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_24)
        }
    }

    dependencies {
        implementation(projects.composeApp)
        implementation(libs.androidx.activity.compose)

        implementation(libs.ktor.client.okhttp)

        //libs.bundles.mockk.android
    }
}