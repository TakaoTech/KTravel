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

//Server Modules
include(":gunzou-api")
project(":gunzou-api").projectDir = file("gunzou/gunzou-api")
include(":gunzou-server")
project(":gunzou-server").projectDir = file("gunzou/gunzou-server")
include(":gunzou-client")
project(":gunzou-client").projectDir = file("gunzou/gunzou-client")
include(":gunzou-server-app")
project(":gunzou-server-app").projectDir = file("gunzou/gunzou-server-app")
