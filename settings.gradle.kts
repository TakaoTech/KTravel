rootProject.name = "ktravel"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }

    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
    }
    versionCatalogs {
        create("ktorLibs").from("io.ktor:ktor-version-catalog:3.5.0")
    }
}

include(":androidApp")
include(":composeApp")
include(":gunzou-here-client")
project(":gunzou-here-client").projectDir = file("gunzou/gunzou-here-client")
include(":password-strength")
// The server modules live under gunzou/ on disk, but keep their flat Gradle paths: every task
// invocation, type-safe accessor and CI reference stays :gunzou-navigator and :gunzou-navigator-app.
include(":gunzou-navigator-api")
project(":gunzou-navigator-api").projectDir = file("gunzou/gunzou-navigator-api")
include(":gunzou-navigator")
project(":gunzou-navigator").projectDir = file("gunzou/gunzou-navigator")
include(":gunzou-navigator-app")
project(":gunzou-navigator-app").projectDir = file("gunzou/gunzou-navigator-app")
