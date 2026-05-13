import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektPlugin
import io.gitlab.arturbosch.detekt.report.ReportMergeTask

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.composeHotReload) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.androidKotlinMultiplatformLibrary) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotest) apply false
    alias(libs.plugins.kotzilla) apply false
    alias(libs.plugins.kotlinx.serialization) apply false
//    alias(libs.plugins.stability.analyzer) apply false
    alias(libs.plugins.mokkery) apply false
    alias(libs.plugins.detekt)
}

val detektReportMergeXml by tasks.registering(ReportMergeTask::class) {
    output.set(rootProject.layout.buildDirectory.file("reports/detekt/merge.xml"))
}

val detektReportMergeMd by tasks.registering(ReportMergeTask::class) {
    output.set(rootProject.layout.buildDirectory.file("reports/detekt/merge.md"))
}

val detektReportMergeSarif by tasks.registering(ReportMergeTask::class) {
    output.set(rootProject.layout.buildDirectory.file("reports/detekt/merge.sarif"))
}

subprojects {
    plugins.withType<DetektPlugin> {
        tasks.withType<Detekt> {
            finalizedBy(detektReportMergeXml, detektReportMergeMd, detektReportMergeSarif)

            detektReportMergeXml.configure {
                input.from(xmlReportFile)
            }
            detektReportMergeMd.configure {
                input.from(mdReportFile)
            }
            detektReportMergeSarif.configure {
                input.from(sarifReportFile)
            }
        }
    }
}

tasks.register("detektAll") {
    group = "verification"
    description = "Runs detekt on all subprojects and merges reports"
    dependsOn(subprojects.flatMap { it.tasks.withType<Detekt>() })
    finalizedBy(detektReportMergeXml, detektReportMergeMd, detektReportMergeSarif)
}
