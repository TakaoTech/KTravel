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
include(":location-clients")
include(":password-strength")
// The server modules live under gunzo/ on disk, but keep their flat Gradle paths: every task
// invocation, type-safe accessor and CI reference stays :gunzo-navigator and :gunzo-navigator-app.
include(":gunzo-navigator")
project(":gunzo-navigator").projectDir = file("gunzo/gunzo-navigator")
include(":gunzo-navigator-app")
project(":gunzo-navigator-app").projectDir = file("gunzo/gunzo-navigator-app")
