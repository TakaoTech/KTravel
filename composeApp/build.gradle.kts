@file:OptIn(ExperimentalMetroGradleApi::class, ExperimentalKotlinGradlePluginApi::class)

import com.android.build.api.dsl.KotlinMultiplatformAndroidCompilation
import com.mikepenz.aboutlibraries.plugin.AboutLibrariesTask
import dev.detekt.gradle.Detekt
import dev.zacsweers.metro.gradle.ExperimentalMetroGradleApi
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.mpp.TestExecutable
import org.w3c.dom.Element
import java.net.URI
import java.util.zip.ZipFile
import javax.xml.parsers.DocumentBuilderFactory

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
    alias(libs.plugins.aboutLibraries)
    alias(libs.plugins.dokka)
    id("kotlin-parcelize")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
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

// ── Couchbase Lite framework (Apple test binaries) ───────────────────────────────────────────────
// kotbase's cinterop klib carries `linkerOpts = -framework CouchbaseLite`. The application resolves
// that through the Swift package referenced by iosApp.xcodeproj, but the test executables are the
// one Apple binary Gradle links itself, with no Xcode around it, so the link ends in
// "ld: framework 'CouchbaseLite' not found". The Objective-C xcframework Couchbase publishes is
// unpacked here and handed to the linker in the target configuration below.
//
// Only the two iOS slices are extracted: the macOS and Mac Catalyst ones are versioned bundles held
// together by symlinks, which a plain zip reader materialises as text stubs holding the target path.
val couchbaseLiteAppleFrameworks = layout.buildDirectory.dir("generated/couchbaseLiteApple")

val fetchCouchbaseLiteAppleFramework by tasks.registering {
    description = "Unpacks the CouchbaseLite framework that the Apple test binaries link against"
    val version = couchbaseLiteVersion
    val archive = layout.buildDirectory.file("tmp/couchbase-lite-objc-$version.zip")
    val outputDir = couchbaseLiteAppleFrameworks
    val slices = listOf("ios-arm64", "ios-arm64_x86_64-simulator")

    inputs.property("couchbaseLiteVersion", version)
    outputs.dir(outputDir)

    doLast {
        val zipFile = archive.get().asFile
        if (!zipFile.exists()) {
            zipFile.parentFile.mkdirs()
            URI(
                "https://packages.couchbase.com/releases/couchbase-lite-ios/$version/" +
                    "couchbase-lite-objc_xc_community_$version.zip",
            ).toURL().openStream().use { input -> zipFile.outputStream().use(input::copyTo) }
        }

        val target = outputDir.get().asFile
        ZipFile(zipFile).use { zip ->
            zip.entries()
                .asSequence()
                .filter { entry ->
                    !entry.isDirectory &&
                        slices.any { entry.name.startsWith("CouchbaseLite.xcframework/$it/") }
                }
                .forEach { entry ->
                    val file = target.resolve(entry.name)
                    file.parentFile.mkdirs()
                    zip.getInputStream(entry).use { input ->
                        file.outputStream().use(input::copyTo)
                    }
                }
        }
    }
}

// Directory holding CouchbaseLite.framework for a given Apple target, as `-F` expects it.
fun couchbaseLiteFrameworkDir(targetName: String) = couchbaseLiteAppleFrameworks.get().asFile
    .resolve("CouchbaseLite.xcframework")
    .resolve(if (targetName == "iosSimulatorArm64") "ios-arm64_x86_64-simulator" else "ios-arm64")

// ── MapLibre iOS system libraries ────────────────────────────────────────────────────────────────
// Since 0.15.0 the iOS map is MapLibre Native FFI, a static library inside the klib, and no longer
// the MapLibre.framework Swift package. A static library carries no link dependencies of its own,
// so the system libraries it was built against have to be named by whatever produces the executable:
// the Xcode project for the app (Other Linker Flags in iosApp.xcodeproj), and the block below for
// the Kotlin test executables.
val MAPLIBRE_IOS_LINKER_FLAGS = arrayOf(
    "-lc++",
    "-lz",
    "-framework", "CoreFoundation",
    "-framework", "CoreGraphics",
    "-framework", "CoreText",
    "-framework", "Foundation",
    "-framework", "ImageIO",
    "-framework", "Metal",
    "-framework", "QuartzCore",
)

// ── MapLibre desktop runtime ─────────────────────────────────────────────────────────────────────
// MapLibre Native FFI ships one runtime artifact per OS/architecture pair, and MapLibre's own
// documentation says to pick the one matching the build host. That is also the only choice that
// makes sense here: packageDistributionForCurrentOS targets the host, and the CI release matrix
// builds one distribution per runner (macos, windows, ubuntu).
//
// macOS x64 is not a supported host: MapLibre publishes no runtime for it, so the map could never
// render. The build refuses it rather than resolving the arm64 runtime and producing a distribution
// that fails at the first frame.
val maplibreDesktopRuntime = run {
    val os = System.getProperty("os.name").orEmpty().lowercase()
    val arch = System.getProperty("os.arch").orEmpty().lowercase()
    val isArm = arch == "aarch64" || arch == "arm64"
    when {
        os.startsWith("mac") -> {
            require(isArm) {
                "macOS x64 is not supported: MapLibre publishes no desktop runtime for it. " +
                    "Build on an arm64 Mac."
            }
            libs.maplibre.runtime.macos.arm64
        }
        os.startsWith("windows") && isArm -> libs.maplibre.runtime.windows.arm64
        os.startsWith("windows") -> libs.maplibre.runtime.windows.x64
        isArm -> libs.maplibre.runtime.linux.arm64
        else -> libs.maplibre.runtime.linux.x64
    }
}

kotlin {
    android {
        namespace = "com.takaotech.ktravel.compose"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()


        // Kept one release behind the toolchain: D8 does not accept the class file version JDK 25
        // emits. The rest of the project targets 25, which the desktop map requires.
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

        // The framework above is static, so its unresolved symbols are the consuming Xcode
        // project's problem — OTHER_LDFLAGS on the iosApp target names the libraries behind them.
        // The test executables are the one Apple binary linked and run for real, so what is
        // behind those symbols has to be spelled out here:
        //   - MapLibre Native FFI ships as a static library inside the klib since 0.15.0. It is
        //     C++ and pulls in the system imaging, text and Metal stacks, which the Kotlin linker
        //     does not add on its own.
        //   - CouchbaseLite, asked for by kotbase's cinterop, is a dynamic framework not provided
        //     at all outside Xcode: it needs the search path (-F) as well as the runtime one
        //     (-rpath), because its install name is @rpath relative.
        iosTarget.binaries.withType<TestExecutable>().configureEach {
            val couchbaseDir = couchbaseLiteFrameworkDir(iosTarget.name).absolutePath
            linkerOpts(*MAPLIBRE_IOS_LINKER_FLAGS)
            linkerOpts("-F$couchbaseDir", "-rpath", couchbaseDir)
            linkTaskProvider.configure { dependsOn(fetchCouchbaseLiteAppleFramework) }
        }
    }

    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_25)
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
                implementation(libs.maplibre.compose)
                implementation(project(":password-strength"))

                // The navigator, and no routing engine anywhere. The client is what the app talks
                // to; the server is only here so it can be started in process, and nothing outside
                // data/navigator imports it. :gunzou-here-client is deliberately absent: hiding it
                // behind the contract is the whole reason the server exists.
                implementation(projects.gunzouClient)
                implementation(projects.gunzouServer)

                // Logging facade of the whole process, configured by core/AppLogging.kt.
                implementation(libs.kermit)

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
                // Nothing downstream of composeApp consumes these, so they stay off the published API.
                implementation(libs.circuit.serialization)
                implementation(libs.circuitx.navigation)
                implementation(libs.circuitx.gesture.navigation)

                implementation(libs.kotlinx.serialization.json)

                implementation(libs.bundles.aboutLibraries)
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
                // Render backend of the Android map (see the catalog entry): runtime only, the
                // compile classpath sees nothing but the shared maplibre-compose API.
                runtimeOnly(libs.maplibre.runtime.android)
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

            // Stands in for the navigator, so the routing provider can be tested without a server
            // and without a routing engine behind it.
            implementation(libs.ktor.client.mock)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.logback.classic)
            runtimeOnly(maplibreDesktopRuntime)
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

val fetchRemoteLicenses = providers.gradleProperty("ktravel.licenses.fetchRemote")
    .map(String::toBooleanStrict)
    .orElse(false)

aboutLibraries {
    offlineMode = fetchRemoteLicenses.map { !it }
    collect {
        fetchRemoteLicense = fetchRemoteLicenses
        gitHubApiToken = providers.environmentVariable("GITHUB_TOKEN")
        includePlatform = true
        includeTargets = true
    }
    library {
        mergePlatformArtifacts = true
    }
}

val aboutLibrariesResources = layout.buildDirectory.dir("generated/aboutLibrariesResources")

val exportLibraryDefinitions = tasks.named<AboutLibrariesTask>("exportLibraryDefinitions") {
    outputDirectory = aboutLibrariesResources
    configureOutputFile(aboutLibrariesResources.map { it.file("files/aboutlibraries.json") })
}

compose.resources {
    listOf("androidMain", "jvmMain", "iosArm64Main", "iosSimulatorArm64Main").forEach { sourceSetName ->
        customDirectory(
            sourceSetName = sourceSetName,
            directoryProvider = exportLibraryDefinitions.map { aboutLibrariesResources.get() },
        )
    }
}

// TODO Check why JVM Toolchain is not applied to compose.desktop
//
// Corretto because jpackage's jlink step and ProGuard both read the `jmods` directory, which several
// distributions (JetBrains Runtime and Temurin among them) leave out. Gradle downloads it through
// the foojay resolver declared in settings.gradle.kts, so JAVA_HOME never matters here.
val desktopPackagingJdk = javaToolchains.launcherFor {
    languageVersion = JavaLanguageVersion.of(25)
    vendor = JvmVendorSpec.AMAZON
}

compose.desktop {
    application {
        mainClass = "com.takaotech.ktravel.MainKt"

        javaHome = desktopPackagingJdk.get().metadata.installationPath.asFile.absolutePath

        jvmArgs(
            // MapLibre Native FFI
            "--enable-native-access=ALL-UNNAMED",
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
            // ProGuard reads the JDK 25 jmods as -libraryjars, so it has to accept class file 69.
            // 7.7.0 stopped at 68; 7.8.0, which Compose 1.12.0 defaults to, raised the ceiling.
            // The pin stays explicit so a Compose downgrade cannot quietly take the ceiling away.
            version.set("7.9.1")
            isEnabled.set(true)
            optimize.set(true)
            obfuscate.set(false)
            // Desktop ProGuard does not read the modules' consumer keep rules: aggregate them here.
            // `$rootDir` avoids a cross-project access, which the configuration cache dislikes.
            //
            // :gunzou-here-client is still listed even though the app no longer compiles against it:
            // the embedded server runs in this process and serialises the HERE DTOs, so their
            // generated serialisers have to survive the shrinker at runtime.
            configurationFiles.from(
                file("$rootDir/gunzou/gunzou-here-client/proguard-consumer-rules.pro"),
                file("$rootDir/gunzou/gunzou-api/proguard-consumer-rules.pro"),
                file("$rootDir/gunzou/gunzou-client/proguard-consumer-rules.pro"),
                file("$rootDir/gunzou/gunzou-server/proguard-consumer-rules.pro"),
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
//   - the specs that open the database: Couchbase Lite fails with "Did you forget to call
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
            "**/AppSettingsRepositoryTest.kt",
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



val checkStringResourceParity by tasks.registering {
    description = "Verifies that every key in values/strings.xml is declared in all translations"
    group = "verification"

    val stringResourceFiles = fileTree(layout.projectDirectory.dir("src/commonMain/composeResources")) {
        include("values*/strings.xml")
    }
    val report = layout.buildDirectory.file("reports/string-resource-parity.txt")
    inputs.files(stringResourceFiles).withPropertyName("stringResources")
    outputs.file(report)

    doLast {
        // Matches %s, %d and their positional forms %1$s, %2$d: a translation that drops or
        // renumbers one formats the wrong argument, or throws, only for the locale that carries it.
        val placeholderPattern = Regex("""%(\d+\$)?[a-zA-Z]""")

        val parse: (File) -> List<Pair<String, Set<String>>> = { file ->
            val children = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(file)
                .documentElement
                .childNodes
            (0 until children.length)
                .map(children::item)
                .filterIsInstance<Element>()
                .filter { it.tagName in setOf("string", "plurals", "string-array") }
                .map { element ->
                    val placeholders = placeholderPattern.findAll(element.textContent)
                        .map(MatchResult::value)
                        .toSet()
                    element.getAttribute("name") to placeholders
                }
        }

        val duplicatesIn: (List<Pair<String, Set<String>>>) -> Set<String> = { entries ->
            entries.groupingBy { it.first }.eachCount().filterValues { it > 1 }.keys
        }

        val files = stringResourceFiles.files.sortedBy { it.parentFile.name }
        val defaultFile = files.singleOrNull { it.parentFile.name == "values" }
            ?: error("src/commonMain/composeResources/values/strings.xml is missing")

        val problems = mutableListOf<String>()
        val summary = mutableListOf<String>()

        val defaultEntries = parse(defaultFile)
        val defaultKeys = defaultEntries.map { it.first }
        val defaultPlaceholders = defaultEntries.toMap()
        duplicatesIn(defaultEntries).forEach { problems += "values: '$it' is declared twice" }
        summary += "values: ${defaultKeys.size} keys (reference)"

        files.filter { it != defaultFile }.forEach { file ->
            val language = file.parentFile.name
            val entries = parse(file)
            val keys = entries.map { it.first }
            val placeholders = entries.toMap()

            duplicatesIn(entries).forEach { problems += "$language: '$it' is declared twice" }
            defaultKeys.minus(keys.toSet()).forEach { problems += "$language: '$it' is missing" }
            keys.minus(defaultKeys.toSet()).forEach {
                problems += "$language: '$it' is not declared in values/strings.xml"
            }
            keys.filter { it in defaultPlaceholders }.forEach { key ->
                val expected = defaultPlaceholders.getValue(key).sorted()
                val actual = placeholders.getValue(key).sorted()
                if (expected != actual) {
                    problems += "$language: '$key' uses $actual where values/strings.xml uses $expected"
                }
            }
            if (keys.toSet() == defaultKeys.toSet() && keys != defaultKeys) {
                logger.warn(
                    "$language/strings.xml declares the same keys as values/strings.xml but in a " +
                        "different order, so the two files do not diff cleanly.",
                )
            }
            summary += "$language: ${keys.size} keys"
        }

        val reportFile = report.get().asFile
        reportFile.parentFile.mkdirs()
        reportFile.writeText(
            (summary + "" + (problems.ifEmpty { listOf("No problems found.") })).joinToString("\n", postfix = "\n"),
        )

        if (problems.isNotEmpty()) {
            throw GradleException(
                problems.joinToString(
                    separator = "\n  - ",
                    prefix = "String resources are out of sync across languages:\n  - ",
                    postfix = "\nSee ${reportFile.absolutePath}",
                ),
            )
        }
    }
}

tasks.named("check") {
    dependsOn(checkStringResourceParity)
}

// src/kzipMain/kotlin is deliberately shared by androidMain, jvmMain and iosMain (see the source
// set wiring above), and Dokka refuses a file that belongs to more than one source set
// (Kotlin/dokka#3701). The jvm copy is the one that gets documented; the others are dropped from
// Dokka's source roots, which leaves the actual declaration documented exactly once.
dokka {
    val kzipSources = layout.projectDirectory.dir("src/kzipMain/kotlin").asFile
    dokkaSourceSets.configureEach {
        if (name != "jvmMain") {
            sourceRoots.setFrom(sourceRoots.files.filterNot { it.startsWith(kzipSources) })
        }
    }
}
