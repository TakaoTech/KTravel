import dev.detekt.gradle.Detekt
import dev.detekt.gradle.plugin.DetektPlugin
import dev.detekt.gradle.report.ReportMergeTask
import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit
import kotlinx.kover.gradle.plugin.dsl.GroupingEntityType

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.androidKotlinMultiplatformLibrary) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotest) apply false
    alias(libs.plugins.metro) apply false
    alias(libs.plugins.kotlinx.serialization) apply false
    alias(libs.plugins.stability.analyzer) apply false
    alias(libs.plugins.mokkery) apply false
    alias(libs.plugins.allopen) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
}

// Coverage aggregation. Every module that carries logic reports into the root project, so
// `./gradlew koverHtmlReport` produces a single cross-module report.
// :androidApp is deliberately absent: it is a framework entry point with no test source set,
// and counting it would only dilute the numbers.
dependencies {
    kover(projects.composeApp)
    kover(projects.locationClients)
    kover(projects.osMap)
}

kover {
    reports {
        filters {
            excludes {
                // Compose Resources accessors, one generated class per drawable/string.
                packages(
                    "ktravel.composeapp.generated.resources",
                    "ktravel.os_map.generated.resources",
                )
                // Compose compiler lambda holders and Metro's generated dependency graphs.
                classes(
                    "*ComposableSingletons*",
                    "*\$\$Metro*",
                    "*.BuildConfig",
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

            reports {
                checkstyle.required.set(true)
                sarif.required.set(true)
            }

            finalizedBy(detektReportMergeXml, detektReportMergeSarif)

            detektReportMergeXml.configure {
                input.from(detektTask.reports.checkstyle.outputLocation)
            }
            detektReportMergeSarif.configure {
                input.from(detektTask.reports.sarif.outputLocation)
            }
        }
    }
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
