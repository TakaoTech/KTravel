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
        maven(url = "https://jitpack.io")
    }
}

include(":androidApp")
include(":composeApp")
include(":os-map")
include(":location-clients")
include(":password-strength")
