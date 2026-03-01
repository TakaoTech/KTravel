import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotest)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.stability.analyzer)
    alias(libs.plugins.detekt)
//    alias(libs.plugins.kotzilla)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    jvm()

//    js {
//        browser {
//            testTask {
//                useKarma {
//                    useFirefox()
//                }
//            }
//        }
//        binaries.executable()
//    }
//
//    @OptIn(ExperimentalWasmDsl::class)
//    wasmJs {
//        browser{
//            testTask {
//                useKarma {
//                    useFirefox()
// //                    useFirefoxHeadless()
// //                    useChromeHeadless()
//                }
//            }
//            val rootDirPath = project.rootDir.path
//            val projectDirPath = project.projectDir.path
//            commonWebpackConfig {
//                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
//                    static = (static ?: mutableListOf()).apply {
//                        // Serve sources to debug inside browser
//                        add(rootDirPath)
//                        add(projectDirPath)
//                    }
//                }
//            }
//        }
//        binaries.executable()
//    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.kotzilla.koin.android)
            implementation(libs.ktor.client.okhttp)
//            implementation(libs.kotzilla.sdk.compose)
        }

        androidUnitTest.dependencies {
            implementation(libs.bundles.mockk.android)
        }

        val commonMain by getting {
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
            dependencies {
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                implementation(libs.androidx.navigationevent)
                implementation(libs.bundles.material.adaptive)
                implementation(libs.compose.ui)
                implementation(libs.compose.resources)
                implementation(libs.compose.preview)
                implementation(libs.compose.constraintlayout)
                implementation(libs.compose.coil)
                implementation(libs.androidx.lifecycle.viewmodelCompose)
                implementation(libs.androidx.lifecycle.runtimeCompose)
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.immutable)
                implementation(project(":os-map"))
                implementation(project(":location-clients"))

                implementation(libs.compottie)
//                implementation(libs.compottie.resources)

                implementation(libs.kotzilla.sdk.compose)
                implementation(libs.kotzilla.koin.core)
                implementation(libs.kotzilla.koin.test)
                implementation(libs.kotzilla.koin.compose)
                implementation(libs.kotzilla.koin.compose.viewmodel)
                implementation(libs.kotzilla.koin.compose.navigation)

                implementation(libs.ktor.client.logging)

                implementation(libs.ktor.client.core)

                implementation(libs.platformtools.core)
                implementation(libs.dnd)
                api(libs.kotzilla.koin.annotation)

                implementation(libs.kotlinx.measure)
                implementation(libs.kotlinx.money)

            }
        }
        iosMain.dependencies {
//            implementation(libs.kotzilla.sdk.compose)
            implementation(libs.ktor.client.darwin)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.bundles.kotest.multiplatform)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.logback.classic)
        }
        jvmTest.dependencies {
            implementation(libs.kotest.runner.junit5)
            implementation(libs.bundles.mockk)
        }
    }
}

android {
    namespace = "com.takaotech.ktravel"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.takaotech.ktravel"
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
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(libs.compose.tooling)
    add("kspCommonMainMetadata", libs.kotzilla.koin.annotation.compiler)
    add("kspAndroid", libs.kotzilla.koin.annotation.compiler)
    add("kspJvm", libs.kotzilla.koin.annotation.compiler)
    add("kspIosArm64", libs.kotzilla.koin.annotation.compiler)
    add("kspIosSimulatorArm64", libs.kotzilla.koin.annotation.compiler)

    detektPlugins(libs.detekt.composerules)
    detektPlugins(libs.detekt.formatting)
}

compose.desktop {
    application {
        mainClass = "com.takaotech.ktravel.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.takaotech.ktravel"
            packageVersion = libs.versions.ktravel.version.get()
        }
    }
}

ksp {
    arg("KOIN_CONFIG_CHECK", "true")
}

// Trigger Common Metadata Generation from Native tasks
tasks.matching {
    it.name.startsWith("ksp") && it.name != "kspCommonMainKotlinMetadata"
}.configureEach {
    dependsOn("kspCommonMainKotlinMetadata")
}

tasks.withType<Test>().configureEach {
    logger.lifecycle("UP-TO-DATE check for $name is disabled, forcing it to run.")
    outputs.upToDateWhen { false }
}

tasks.named<Test>("jvmTest") {
    useJUnitPlatform()
    filter {
        isFailOnNoMatchingTests = false
    }
}

// kotzilla {
//    versionName = libs.versions.ktravel.version.get()
//    keyGeneration = KotzillaKeyGeneration.NONE
//    composeInstrumentation = true
// }

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

    // Ensure detekt tasks run after KSP generates code
    mustRunAfter(tasks.matching { it.name == "kspCommonMainKotlinMetadata" })
}
