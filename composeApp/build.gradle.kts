@file:OptIn(ExperimentalMetroGradleApi::class, ExperimentalKotlinGradlePluginApi::class)

import com.android.build.api.dsl.KotlinMultiplatformAndroidCompilation
import dev.zacsweers.metro.gradle.ExperimentalMetroGradleApi
import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotest)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.stability.analyzer)
    alias(libs.plugins.detekt)
    alias(libs.plugins.mokkery)
    alias(libs.plugins.metro)
    alias(libs.plugins.allopen)
    id("kotlin-parcelize")
}

kotlin {
    android {
        namespace = "com.takaotech.ktravel.compose"
        compileSdk = libs.versions.android.compileSdk.get().toInt()


        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }

        androidResources {
            enable = true
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

    applyDefaultHierarchyTemplate {
        common {
            group("jvmAndroid") {
                withJvm()
                withCompilations { it is KotlinMultiplatformAndroidCompilation }
            }
        }
    }

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
//        androidMain.dependencies {
//            implementation(libs.androidx.activity.compose)
//            implementation(libs.kotzilla.koin.android)
//            implementation(libs.ktor.client.okhttp)
////            implementation(libs.kotzilla.sdk.compose)
//        }

//        androidUnitTest.dependencies {
//            implementation(libs.bundles.mockk.android)
//        }

        val commonMain by getting {
            dependencies {
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                implementation(libs.compose.backhandler.core)
                implementation(libs.navigation.compose)
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

                // Nota: Hyphen (editor Markdown) non pubblica artefatti iOS: aggiunto solo ai
                // sourceSet android/jvm; su iOS si usa un fallback (vedi MarkdownNoteEditor).
                implementation(libs.markdown.renderer)
                implementation(libs.markdown.renderer.m3)

                implementation(libs.metro.runtime)
                implementation(libs.metro.viewmodel)
                implementation(libs.metro.viewmodel.compose)

                implementation(libs.ktor.client.logging)
                implementation(libs.ktor.client.core)

                implementation(libs.platformtools.core)
                implementation(libs.dnd)

                implementation(libs.kotlinx.measure)
                implementation(libs.kotlinx.money)

                implementation(libs.filekit.core)
                implementation(libs.filekit.dialogs)
                implementation(libs.filekit.dialogs.compose)

                implementation(libs.couchbase.lite)

                // L'astrazione zip (data/archive/zip) parla kotlinx.io.files.Path: dichiarato
                // esplicitamente per non dipendere dalla versione che filekit trascina.
                implementation(libs.kotlinx.io.core)

                api(libs.circuit.foundation)
                api(libs.circuit.runtime)
                api(libs.circuit.runtime.presenter)
                api(libs.circuit.runtime.ui)

                implementation(libs.kotlinx.serialization.json)
            }
        }

        val jvmAndroidMain by getting {
            dependencies {
                //Source set sharede with And-JVM-JS, currently no iOS
                implementation(libs.hyphen)
            }
        }

        // kzip non ha una variante androidJvm, quindi la dipendenza non può stare in un source set
        // intermedio (risolto come metadata): la si dichiara nei source set leaf, usando l'artefatto
        // jvm su Android/Desktop (regola di compatibilità KGP jvm -> androidJvm) e il modulo KMP su
        // iOS. L'API è identica, quindi l'unica implementazione `actual` è condivisa via srcDir.
        androidMain {
            kotlin.srcDir("src/kzipMain/kotlin")
            dependencies {
                implementation(libs.kzip.jvm)
            }
        }
        jvmMain {
            kotlin.srcDir("src/kzipMain/kotlin")
            dependencies {
                implementation(libs.kzip.jvm)
            }
        }
        iosMain {
            kotlin.srcDir("src/kzipMain/kotlin")
            dependencies {
//                implementation(libs.kotzilla.sdk.compose)
                implementation(libs.ktor.client.darwin)
                implementation(libs.kzip)
            }
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.bundles.kotest.multiplatform)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.compose.ui.test)
            implementation(libs.circuit.test)
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

        targets.configureEach {
            if (platformType == KotlinPlatformType.androidJvm) {
                compilations.configureEach {
                    compileTaskProvider.configure {
                        compilerOptions {
                            freeCompilerArgs.addAll(
                                "-P",
                                "plugin:org.jetbrains.kotlin.parcelize:additionalAnnotation=com.takaotech.ktravel.core.annotation.Parcelize",
                            )
                        }
                    }
                }
            }
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.tooling)

    detektPlugins(libs.detekt.composerules)
    detektPlugins(libs.detekt.formatting)
}

metro {
    enabled = true
    debug = false
    enableCircuitCodegen = true
}

compose.desktop {
    application {
        mainClass = "com.takaotech.ktravel.MainKt"

        jvmArgs(
            "--add-opens=java.base/java.lang=ALL-UNNAMED",
            "--add-opens=java.desktop/sun.awt=ALL-UNNAMED",
            "--add-opens=java.desktop/sun.java2d=ALL-UNNAMED",
            "--add-opens=java.desktop/java.awt.peer=ALL-UNNAMED",
            "--add-opens=java.desktop/sun.lwawt=ALL-UNNAMED",
            "--add-opens=java.desktop/sun.lwawt.macosx=ALL-UNNAMED",
            "--add-exports=java.desktop/sun.awt=ALL-UNNAMED",
            "--add-exports=java.desktop/sun.lwawt=ALL-UNNAMED",
            "--add-exports=java.desktop/sun.lwawt.macosx=ALL-UNNAMED"
        )

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.takaotech.ktravel"
            packageVersion = libs.versions.ktravel.version.get()
        }
    }
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

allOpen {
    annotation("com.takaotech.ktravel.core.annotation.OpenForMokkery")
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
    // Detekt 1.23.x gira in-process nel daemon Gradle e non supporta JVM 23+: va lanciato con un
    // JDK <= 22 (`./gradlew detekt` con JAVA_HOME su JDK 21), oppure aggiornando detekt.
    jvmTarget = JvmTarget.JVM_21.target

    exclude("**/build/**", "**/generated/**", "org/koin/ksp/generated/**")
    reports {
        md.required.set(true)
        xml.required.set(true)
//        html.outputLocation.set(file("$rootDir/reports/detekt/composeApp.html"))
    }

}

