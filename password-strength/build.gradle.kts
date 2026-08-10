import dev.detekt.gradle.Detekt
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

/**
 * Turns the word lists in `dictionaries/` into Kotlin sources.
 *
 * Compose Resources would have been shorter, but it does not cover the Linux and Windows native
 * targets this module builds for. Generated constants work everywhere and need no `expect`/`actual`.
 *
 * Each list is emitted as one object holding the file as a sequence of string constants. The split
 * exists because the JVM constant pool caps a single UTF-8 constant at 65535 *bytes*: the chunk size
 * below is in characters, and is small enough that even three-byte characters stay under the cap.
 */
abstract class GenerateDictionarySources : DefaultTask() {

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val dictionaries: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun generate() {
        val outputRoot = outputDirectory.get().asFile
        outputRoot.deleteRecursively()
        val packageDir = outputRoot.resolve(PACKAGE.replace('.', '/'))
        packageDir.mkdirs()

        dictionaries.get().asFile.listFiles { file -> file.extension == "txt" }
            .orEmpty()
            .sortedBy { it.name }
            .forEach { source ->
                val objectName = source.nameWithoutExtension.toObjectName()
                packageDir.resolve("$objectName.kt")
                    .writeText(render(objectName, source.name, source.readText()))
            }
    }

    private fun render(objectName: String, fileName: String, content: String): String {
        val chunks = content.chunked(CHUNK_SIZE)
        return buildString {
            appendLine("// Generated from $fileName by the generateDictionarySources task. Do not edit.")
            appendLine("// Word list from nbvcxz (MIT) — see LICENSE-nbvcxz.txt.")
            appendLine("package $PACKAGE")
            appendLine()
            appendLine("internal object $objectName {")
            chunks.forEachIndexed { index, chunk ->
                appendLine("    private const val C$index: String = \"${chunk.escaped()}\"")
            }
            appendLine()
            appendLine("    /** One entry per line, in the order the upstream file lists them. */")
            appendLine("    fun lines(): List<String> = buildString(${content.length}) {")
            chunks.indices.forEach { index -> appendLine("        append(C$index)") }
            appendLine("    }.lineSequence().filter(String::isNotEmpty).toList()")
            appendLine("}")
        }
    }

    /** `female-names` -> `FemaleNamesDictionary`, `eff_large` -> `EffLargeDictionary`. */
    private fun String.toObjectName(): String = split('-', '_')
        .joinToString("") { part -> part.replaceFirstChar(Char::uppercaseChar) }
        .plus("Dictionary")

    private fun String.escaped(): String = buildString(length) {
        this@escaped.forEach { character ->
            when (character) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '$' -> append("\\$")
                '\n' -> append("\\n")
                '\r' -> Unit
                else -> append(character)
            }
        }
    }

    private companion object {
        const val PACKAGE = "com.takaotech.password.resources"

        /**
         * In characters. Three bytes per character worst case leaves the constant well under the
         * 65535-byte JVM limit.
         */
        const val CHUNK_SIZE = 16_000
    }
}

val generateDictionarySources by tasks.registering(GenerateDictionarySources::class) {
    group = "build"
    description = "Generates Kotlin sources from the word lists in dictionaries/"
    dictionaries.set(layout.projectDirectory.dir("dictionaries"))
    outputDirectory.set(layout.buildDirectory.dir("generated/dictionaries"))
}

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    // Required by the Kotest plugin, which generates the per-target spec registration via KSP.
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotest)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

kotlin {
    // The port is plain Kotlin with no dependency beyond the standard library, which is what lets
    // this module target everything Kotlin can build. Adding a dependency here means checking it
    // ships artifacts for every target below.
    android {
        namespace = "com.takaotech.password"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_24)
        }

        withHostTestBuilder { }.configure {
            isReturnDefaultValues = true
        }

        // Keep rules shipped to consumers that minify (see androidApp). `publish` is required:
        // consumer rules of a KMP library are not published by default.
        optimization {
            consumerKeepRules.publish = true
            consumerKeepRules.file("proguard-consumer-rules.pro")
        }
    }

    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_25)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
        iosX64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "PasswordStrengthKit"
            isStatic = true
        }
    }

    // macosX64 is deliberately absent: it is a deprecated target scheduled for removal.
    macosArm64()
    watchosArm64()
    watchosSimulatorArm64()
    tvosArm64()
    tvosSimulatorArm64()
    linuxX64()
    linuxArm64()
    mingwX64()

    js {
        browser()
        nodejs()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        nodejs()
    }

    sourceSets {
        // Passing the task provider (not a path) is what makes every compilation depend on it.
        commonMain {
            kotlin.srcDir(generateDictionarySources)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.bundles.kotest.multiplatform)
        }

        jvmTest.dependencies {
            implementation(libs.kotest.runner.junit5)
        }

        // Mirrors jvmTest: the JUnit Platform runner that discovers the Kotest specs on the Android
        // host test compilation. Created by withHostTestBuilder above, so there is no accessor.
        getByName("androidHostTest").dependencies {
            implementation(libs.kotest.runner.junit5)
        }
    }
}

// Kotest runs on the JUnit Platform; without this the specs are not discovered at all.
tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    filter {
        isFailOnNoMatchingTests = false
    }
}

dependencies {
    // There is no Compose in this module, but the shared detekt.yml carries the `Compose` rule
    // section: without the plugin that owns it, detekt rejects the whole config as invalid.
    detektPlugins(libs.detekt.composerules)
    detektPlugins(libs.detekt.formatting)
}

detekt {
    ignoreFailures = true
    config.setFrom(file("$rootDir/config/detekt/detekt.yml"))

    arrayOf(
        "commonMain",
    ).map {
        "src/$it/kotlin"
    }.let {
        source.setFrom(it)
    }
}

tasks.withType<Detekt>().configureEach {
    exclude("**/build/**", "**/generated/**")
    reports {
        markdown.required.set(true)
    }
}
