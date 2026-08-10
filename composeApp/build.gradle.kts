@file:OptIn(ExperimentalMetroGradleApi::class, ExperimentalKotlinGradlePluginApi::class)

import com.android.build.api.dsl.KotlinMultiplatformAndroidCompilation
import dev.detekt.gradle.Detekt
import dev.zacsweers.metro.gradle.ExperimentalMetroGradleApi
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import java.net.URI
import java.util.zip.ZipFile

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
    alias(libs.plugins.kover)
    alias(libs.plugins.mokkery)
    alias(libs.plugins.metro)
    alias(libs.plugins.allopen)
    id("kotlin-parcelize")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(24)
    }
}

// ── Couchbase Lite native prerequisites (Linux desktop) ──────────────────────────────────────────
// libLiteCore.so, the engine behind Couchbase Lite, links against ICU 71. No Ubuntu LTS ships that
// major — 22.04 has 70, 24.04 has 74 — and ICU exports version suffixed symbols (u_strlen_71), so a
// symlink pointing at another major resolves the file and then fails on the symbols. Rather than
// making every Linux user install the libraries by hand, they are unpacked from the archive
// Couchbase builds against and packaged as application resources: the distribution carries its own
// copy, and the test run loads it exactly the way the shipped application does.
//
// Only produced when building on Linux. packageDistributionForCurrentOS targets the host, so a
// macOS or Windows bundle would carry 69 MB it can never use, and the loader is a no-op there.
val isLinuxHost = System.getProperty("os.name").orEmpty().startsWith("Linux")

val couchbaseLiteVersion = libs.versions.kotbase.get().substringBefore('-')

val fetchCouchbaseIcuLibraries by tasks.registering {
    description = "Unpacks the ICU 71 libraries that Couchbase Lite's libLiteCore.so links against"
    val version = couchbaseLiteVersion
    val archive = layout.buildDirectory.file("tmp/couchbase-supportlibs-$version.zip")
    val outputDir = layout.buildDirectory.dir("generated/couchbaseIcu")
    // Named after the SONAME each library is loaded under, which is what libLiteCore.so asks for.
    // The archive also holds those names as symlinks to the .71.1 files; a plain zip reader would
    // materialise them as text stubs holding the target path, so the real entries are read instead.
    val libraries = listOf("libicudata.so.71", "libicuuc.so.71", "libicui18n.so.71")

    inputs.property("couchbaseLiteVersion", version)
    outputs.dir(outputDir)

    doLast {
        val zipFile = archive.get().asFile
        if (!zipFile.exists()) {
            zipFile.parentFile.mkdirs()
            URI(
                "https://packages.couchbase.com/releases/couchbase-lite-java/$version/" +
                    "couchbase-lite-java-linux-supportlibs-$version.zip",
            ).toURL().openStream().use { input -> zipFile.outputStream().use(input::copyTo) }
        }

        val target = outputDir.get().asFile.resolve("couchbase/icu")
        target.mkdirs()
        ZipFile(zipFile).use { zip ->
            libraries.forEach { soname ->
                val entry = requireNotNull(zip.getEntry("$soname.1")) {
                    "$soname.1 is missing from the Couchbase Lite support libs archive"
                }
                zip.getInputStream(entry).use { input ->
                    target.resolve(soname).outputStream().use(input::copyTo)
                }
            }
        }
    }
}

kotlin {
    android {
        namespace = "com.takaotech.ktravel.compose"
        compileSdk = libs.versions.android.compileSdk.get().toInt()


        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_24)
        }

        androidResources {
            enable = true
        }

        // Runs the commonTest specs on the Android JVM as well, so the shared logic is verified
        // against the Android variant and not only against the desktop one. Kotest specs are
        // discovered through the JUnit Platform, which is why no per-class annotation is needed:
        // instrumented tests would require @RunWith(KotestTestRunner::class), an annotation that
        // cannot live in commonTest because that source set also compiles for iOS.
        withHostTestBuilder { }.configure {
            isReturnDefaultValues = true
            isIncludeAndroidResources = true
        }

        // Keep rules shipped to consumers that minify (see androidApp). `publish` is required:
        // consumer rules of a KMP library are not published by default.
        // The desktop counterpart lives in proguard-desktop-rules.pro, wired below.
        optimization {
            consumerKeepRules.publish = true
            consumerKeepRules.file("proguard-consumer-rules.pro")
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

    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_24)
        }
    }

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
                implementation(project(":password-strength"))

                // Archive secrets: scrypt key derivation and AES-256-GCM (see data/archive/crypto).
                implementation(libs.signum.indispensable)
                implementation(libs.signum.supreme)

                implementation(libs.compottie)

                // Note: Hyphen (the Markdown editor) publishes no iOS artefacts, so it is added to
                // the android/jvm source sets only; iOS uses a fallback (see MarkdownNoteEditor).
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

                // The zip abstraction (data/archive/zip) speaks kotlinx.io.files.Path: declared
                // explicitly so it does not depend on whatever version filekit drags in.
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

        // kzip has no androidJvm variant, so the dependency cannot sit in an intermediate source
        // set (resolved as metadata): it is declared in the leaf source sets, using the jvm artefact
        // on Android/Desktop (KGP jvm -> androidJvm compatibility rule) and the KMP module on iOS.
        // The API is identical, so the single `actual` implementation is shared through srcDir.
        androidMain {
            kotlin.srcDir("src/kzipMain/kotlin")
            dependencies {
                implementation(libs.kzip.jvm)
            }
        }
        jvmMain {
            kotlin.srcDir("src/kzipMain/kotlin")
            // Packaged into the jar, which puts them on the distribution's classpath and, just as
            // importantly, on the one jvmTest runs against: the suites that open the database load
            // ICU through the same code path as the shipped application, with nothing installed on
            // the machine around them.
            if (isLinuxHost) {
                resources.srcDir(fetchCouchbaseIcuLibraries)
            }
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
        // Mirrors jvmTest: the JUnit Platform runner that discovers the Kotest specs, plus the
        // Android flavour of mockk (the plain artifact cannot instrument the Android JVM).
        // Created by withHostTestBuilder above, so there is no generated accessor for it.
        getByName("androidHostTest").dependencies {
            implementation(libs.kotest.runner.junit5)
            implementation(libs.bundles.mockk.android)
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

// TODO Check why JVM Toolchain is not applied to compose.desktop
val desktopPackagingJdk = javaToolchains.launcherFor {
    languageVersion = JavaLanguageVersion.of(24)
    vendor = JvmVendorSpec.AMAZON
}

compose.desktop {
    application {
        mainClass = "com.takaotech.ktravel.MainKt"

        javaHome = desktopPackagingJdk.get().metadata.installationPath.asFile.absolutePath

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


        buildTypes.release.proguard {
            isEnabled.set(true)
            optimize.set(true)
            obfuscate.set(false)
            // Desktop ProGuard does not read the modules' consumer keep rules: aggregate them here.
            // `$rootDir` avoids a cross-project access, which the configuration cache dislikes.
            configurationFiles.from(
                file("$rootDir/location-clients/proguard-consumer-rules.pro"),
                file("$rootDir/os-map/proguard-consumer-rules.pro"),
                file("$rootDir/os-map/proguard-desktop-rules.pro"),
                file("proguard-consumer-rules.pro"),
                file("proguard-desktop-rules.pro"),
            )
        }
    }
}

// commonTest also compiles for the Android host test compilation, but a local unit test has no
// real Android runtime behind it: there is no Robolectric here, and Kotest specs cannot opt into
// it because @RunWith / @RobolectricTest are JVM-only annotations that commonTest — which also
// compiles for iOS — cannot carry. The two groups below need that runtime and are therefore
// verified on the JVM target only:
//   - ui/**: Compose UI tests read android.os.Build.FINGERPRINT to pick an idling strategy,
//     which is null without Robolectric.
//   - the three specs that open the database: Couchbase Lite fails with "Did you forget to call
//     CouchbaseLite.init()?" because the Android artifact needs a Context to initialise.
// Everything else — domain, presentation, mappers, the rest of data — runs on both targets.
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    if (name == "compileAndroidHostTest") {
        exclude(
            "**/ui/**",
            "**/TravelArchiveRoundTripTest.kt",
            "**/TravelArchiveSecretsRoundTripTest.kt",
            "**/TravelArchiveCorruptionTest.kt",
            "**/TravelPlanStorageDataSourceImplTest.kt",
        )
    }
}

// Covers jvmTest and testAndroidHostTest alike. `tasks.named("testAndroidHostTest")` is not an
// option: with AGP 9 that name is not resolvable at configuration time.
tasks.withType<Test>().configureEach {
    // Kotest runs on the JUnit Platform; without this the specs are not discovered at all.
    useJUnitPlatform()
    filter {
        isFailOnNoMatchingTests = false
    }
    logger.lifecycle("UP-TO-DATE check for $name is disabled, forcing it to run.")
    outputs.upToDateWhen { false }
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
    exclude("**/build/**", "**/generated/**", "org/koin/ksp/generated/**")
    reports {
        markdown.required.set(true)
//        html.outputLocation.set(file("$rootDir/reports/detekt/composeApp.html"))
    }

}

