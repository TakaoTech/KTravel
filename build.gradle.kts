import dev.detekt.gradle.Detekt
import dev.detekt.gradle.plugin.DetektPlugin
import dev.detekt.gradle.report.ReportMergeTask
import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit
import kotlinx.kover.gradle.plugin.dsl.GroupingEntityType
import org.jetbrains.dokka.gradle.DokkaExtension
import org.jetbrains.dokka.gradle.DokkaPlugin

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.androidKotlinMultiplatformLibrary) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotest) apply false
    alias(libs.plugins.metro) apply false
    alias(libs.plugins.kotlinx.serialization) apply false
    alias(libs.plugins.stability.analyzer) apply false
    alias(libs.plugins.mokkery) apply false
    alias(libs.plugins.allopen) apply false
    alias(libs.plugins.aboutLibraries) apply false
    alias(libs.plugins.kotzilla) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
    alias(libs.plugins.sonarqube)
    alias(libs.plugins.dokka)
}

// SonarCloud analysis. The three reports it consumes are produced by tasks that already exist:
// :koverXmlReport (JaCoCo schema), :detektReportMergeXml (checkstyle schema) and :androidApp:lint
// (Android Lint schema). CI runs them first, then `./gradlew sonar --no-configuration-cache` — the
// scanner plugin does not support the configuration cache, which this build enables by default.
//
// The paths are absolute and declared on the root project: the scanner treats root properties as
// defaults inherited by every module, so each module picks the issues that belong to its own files
// out of the same report.
sonar {
    properties {
        property("sonar.projectKey", "TakaoTech_KTravel")
        property("sonar.organization", "takaotech")
        property("sonar.host.url", "https://sonarcloud.io")
        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            layout.buildDirectory.file("reports/kover/report.xml").get().asFile.path,
        )
        property(
            "sonar.kotlin.detekt.reportPaths",
            layout.buildDirectory.file("reports/detekt/merge.xml").get().asFile.path,
        )

        property(
            "sonar.androidLint.reportPaths",
            project(":androidApp").layout.buildDirectory
                .file("reports/lint-results-debug.xml").get().asFile.path,
        )
        // The reports come from dedicated tasks upstream; letting the scanner recompile is waste.
        property("sonar.gradle.skipCompile", "true")
    }
}
// Source sets are left to the scanner's own KMP detection, which resolves them correctly here
// (commonMain, androidMain, jvmMain, iosMain, the intermediate ones, and commonTest as tests).
// Scanner 7.3.1 has an open bug, SCANGRADLE-429, that registers Android source sets twice on KMP
// modules and kills the analysis with "can't be indexed twice" — verified as not triggering on
// this project, because androidHostTest carries no files of its own and only inherits commonTest.
// Should someone add sources under src/androidHostTest, re-check with:
//   ./gradlew sonar --no-configuration-cache -Dsonar.scanner.internal.dumpToFile=/tmp/props.txt

// Coverage aggregation. Every module that carries logic reports into the root project, so
// `./gradlew koverHtmlReport` produces a single cross-module report.
// :androidApp is deliberately absent: it is a framework entry point with no test source set,
// and counting it would only dilute the numbers.
dependencies {
    kover(projects.composeApp)
    kover(projects.gunzouHereClient)
    kover(projects.passwordStrength)
    kover(projects.gunzouServer)
    kover(projects.gunzouApi)
    kover(projects.gunzouClient)

    // API documentation aggregation, on the same module list as the coverage one above and for
    // the same reason: :androidApp and :gunzou-server-app are shells (the APK entry point and the
    // JVM packaging module) with no public API of their own to document.
    dokka(projects.composeApp)
    dokka(projects.gunzouHereClient)
    dokka(projects.passwordStrength)
    dokka(projects.gunzouServer)
    dokka(projects.gunzouApi)
    dokka(projects.gunzouClient)
}

// ── Logging backends ─────────────────────────────────────────────────────────────────────────────
// One SLF4J backend per application, and never log4j.
//
// log4j is on no classpath of this repository today, and these exclusions are what keeps a
// transitive dependency from quietly putting it back. logback is excluded from the application
// only: there the SLF4J backend is Kermit (core/logging/slf4j), and a second provider would make
// which one wins a matter of classpath order. The standalone :gunzou-server-app keeps logback, and
// its logback.xml with it.
subprojects {
    configurations.configureEach {
        exclude(group = "log4j")
        exclude(group = "org.apache.logging.log4j")
        exclude(group = "org.slf4j", module = "slf4j-simple")

        if (project.path in listOf(":composeApp", ":androidApp")) {
            exclude(group = "ch.qos.logback")
        }
    }

    tasks.matching { it.name.startsWith("ksp") }.configureEach {
        dependsOn(tasks.matching { it.name.startsWith("generateKotzillaConfig") })
    }
}

// Shared Dokka configuration. It lives here rather than in a convention plugin because a
// convention plugin has to come from buildSrc, and the Dokka plugin loaded by the buildSrc
// classloader cannot see the Kotlin plugin loaded by the build scripts: its KotlinAdapter then
// fails with "could not load KotlinBasePlugin" and documents nothing at all.
subprojects {
    plugins.withType<DokkaPlugin> {
        // The remote source root is pinned to the default branch rather than to a tag: the
        // documentation is regenerated on every push to it, so the links stay in step with the
        // sources they point at.
        val sourceRootUrl = "https://github.com/TakaoTech/KTravel/tree/dev"
        val moduleRelativePath = projectDir.relativeTo(rootDir).invariantSeparatorsPath

        extensions.configure<DokkaExtension> {
            dokkaSourceSets.configureEach {
                // Compose Resources accessors, one generated declaration per drawable and per
                // string. The Kover filters above exclude the very same package.
                perPackageOption {
                    matchingRegex.set("""ktravel\.composeapp\.generated\.resources.*""")
                    suppress.set(true)
                }

                sourceLink {
                    localDirectory.set(projectDir)
                    remoteUrl("$sourceRootUrl/$moduleRelativePath")
                    remoteLineSuffix.set("#L")
                }
            }
        }
    }
}

kover {
    reports {
        filters {
            excludes {
                // Compose Resources accessors, one generated class per drawable/string.
                packages("ktravel.composeapp.generated.resources")
                // Compose compiler lambda holders and Metro's generated dependency graphs.
                classes(
                    "*ComposableSingletons*",
                    $$$"*$$Metro*",
                    "*.BuildConfig",
                    // Preview fixtures: sample data, not covered behaviour. The previews themselves
                    // are excluded by annotation below, wherever the file they sit in.
                    "*PreviewDataKt",
                )
                annotatedBy(
                    "*Generated*",
                    "androidx.compose.ui.tooling.preview.Preview",
                    "org.jetbrains.compose.ui.tooling.preview.Preview",
                )
            }
        }

        total {
            html {
                title = "KTravel coverage"
                onCheck = false
            }
            xml {
                onCheck = false
            }
            // Prints the aggregated line coverage to the console after a report run.
            log {
                onCheck = false
                header = "KTravel coverage"
                groupBy = GroupingEntityType.APPLICATION
                coverageUnits = CoverageUnit.LINE
                aggregationForGroup = AggregationType.COVERED_PERCENTAGE
            }
        }
    }
}

// ReportMergeTask only knows how to merge checkstyle (xml) and sarif reports; markdown is
// available per module but cannot be merged.
val detektReportMergeXml by tasks.registering(ReportMergeTask::class) {
    output.set(rootProject.layout.buildDirectory.file("reports/detekt/merge.xml"))
}

val detektReportMergeSarif by tasks.registering(ReportMergeTask::class) {
    output.set(rootProject.layout.buildDirectory.file("reports/detekt/merge.sarif"))
}

subprojects {
    plugins.withType<DetektPlugin> {
        tasks.withType<Detekt>().configureEach {
            val detektTask = this

            // Only the ktlint rule set auto corrects; the default detekt rules never rewrite code.
            // Driven by a property so CI can format with -Pdetekt.autocorrect=true while a local
            // run stays read-only.
            val autoCorrectEnabled = providers.gradleProperty("detekt.autocorrect")
                .map(String::toBoolean)
                .orElse(false)
            autoCorrect.set(autoCorrectEnabled)
            // When correcting, the task rewrites its own inputs: an UP-TO-DATE result would be a lie.
            outputs.upToDateWhen { !autoCorrectEnabled.get() }

            // detekt only indexes .kt files under the source dirs configured per module, so the
            // minified lottie_*.json animations are already out of reach. Stated explicitly so a
            // future change to `source` cannot silently start reformatting them.
            exclude("**/lottie_*.json")

            // The per-source-set detekt tasks take their roots from the Kotlin source sets, which
            // include the KSP output directories. A path pattern cannot catch those: `exclude` is
            // matched against the path *relative to each root*, and for a root that already sits
            // inside build/ that relative path never mentions "build". Hence the file-level spec.
            exclude { it.file.invariantSeparatorsPath.contains("/build/") }

            reports {
                checkstyle.required.set(true)
                sarif.required.set(true)
            }

            // The merge tasks consume the report of *every* Detekt task, so finalizing by them
            // pulls the whole family into the graph — including the type-resolving compilation
            // tasks that must not run while sources are being rewritten (see :detektFormat).
            // While correcting, the reports are worthless anyway: the run that matters is the
            // analysis one, which happens without this flag.
            if (!autoCorrectEnabled.get()) {
                finalizedBy(detektReportMergeXml, detektReportMergeSarif)
            }

            detektReportMergeXml.configure {
                input.from(detektTask.reports.checkstyle.outputLocation)
            }
            detektReportMergeSarif.configure {
                input.from(detektTask.reports.sarif.outputLocation)
            }
        }
    }
}

// Single entry point for the API documentation, mirroring :detektAll below.
//
// The aggregating task is the root project's own dokkaGenerate: it builds the module output of
// each of the six documented modules and merges them into build/dokka/html. Reaching it means
// writing `:dokkaGenerate` with the leading colon, because the unqualified `dokkaGenerate` runs
// the task in every project and so additionally builds a standalone publication per module, which
// nothing consumes. This alias removes that trap.
tasks.register("dokkaAll") {
    group = "documentation"
    description = "Generates the aggregated API documentation of every documented module"
    dependsOn(tasks.named("dokkaGenerate"))
}

tasks.register("detektAll") {
    group = "verification"
    description = "Runs detekt on all subprojects and merges reports"
    // Pass the live TaskCollections instead of flat-mapping them: flattening realizes every
    // Detekt task while :detektAll is itself being created, which forbids configuring the
    // merge tasks from tasks.withType<Detekt>().configureEach.
    dependsOn(subprojects.map { it.tasks.withType<Detekt>() })
    finalizedBy(detektReportMergeXml, detektReportMergeSarif)
}

// Formatting entry point, kept apart from: detektAll on purpose.
//
// The plugin registers two families of Detekt tasks: one per Kotlin source set (purely syntactic)
// and one per compilation (`detektMainJvm`, `detektMainAndroid`, ...) which resolves types against
// the FIR model built by the Kotlin compiler. Correcting and type-resolving in the same build is a
// contradiction: as soon as a source-set task rewrites a file, the FIR model the compilation tasks
// hold no longer matches the file on disk, and they die with
//   "FirDeclaration was not found for class org.jetbrains.kotlin.psi.KtFunctionLiteral, fir is null".
// The ktlint rules are all syntactic, so nothing is lost by leaving type resolution out here.
//
// CI formats with `./gradlew detektFormat -Pdetekt.autocorrect=true` and analyses in a separate
// job with `./gradlew detektAll`, on sources that are already formatted.
tasks.register("detektFormat") {
    group = "formatting"
    description =
        "Applies the auto correctable (ktlint) rules; pair it with -Pdetekt.autocorrect=true"
    dependsOn(
        subprojects.map { project ->
            project.tasks.withType<Detekt>().matching { it.name.endsWith("SourceSet") }
        },
    )
}
