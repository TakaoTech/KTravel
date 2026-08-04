#!/usr/bin/env kotlin

@file:Suppress("MaxLineLength")

@file:Repository("https://repo.maven.apache.org/maven2/")
@file:DependsOn("io.github.typesafegithub:github-workflows-kt:3.7.0")
@file:Repository("https://bindings.krzeminski.it")

@file:DependsOn("actions:checkout:v4")
@file:DependsOn("actions:setup-java:v4")
@file:DependsOn("actions:upload-artifact:v4")
@file:DependsOn("gradle:actions__setup-gradle:v4")

import io.github.typesafegithub.workflows.actions.actions.Checkout
import io.github.typesafegithub.workflows.actions.actions.SetupJava
import io.github.typesafegithub.workflows.actions.actions.UploadArtifact
import io.github.typesafegithub.workflows.actions.gradle.ActionsSetupGradle
import io.github.typesafegithub.workflows.domain.RunnerType
import io.github.typesafegithub.workflows.domain.Shell
import io.github.typesafegithub.workflows.domain.triggers.Push
import io.github.typesafegithub.workflows.domain.triggers.WorkflowDispatch
import io.github.typesafegithub.workflows.dsl.expressions.expr
import io.github.typesafegithub.workflows.dsl.workflow
import io.github.typesafegithub.workflows.yaml.ConsistencyCheckJobConfig

val jdkVersion = "24"

workflow(
    name = "Release",
    on = listOf(
        WorkflowDispatch(),
        Push(tags = listOf("v*")),
    ),
    sourceFile = __FILE__,
    consistencyCheckJobConfig = ConsistencyCheckJobConfig.Disabled
) {
    // Gate for both build jobs: no artifact is produced from a tree whose tests do not pass.
    // Same suites as the CI workflow, on both JVM flavours.
    val test = job(id = "test", runsOn = RunnerType.UbuntuLatest) {
        uses(name = "Checkout code", action = Checkout())
        uses(
            name = "Set up JDK",
            action = SetupJava(
                distribution = SetupJava.Distribution.Corretto,
                javaVersion = jdkVersion
            )
        )
        uses(name = "Setup Gradle", action = ActionsSetupGradle())

        run(
            name = "Grant permission to execute gradlew",
            command = "chmod +x gradlew"
        )

        run(
            name = "Run tests",
            command = "./gradlew jvmTest testAndroidHostTest"
        )
    }

    job(id = "android", runsOn = RunnerType.UbuntuLatest, needs = listOf(test)) {
        uses(name = "Checkout code", action = Checkout())
        uses(
            name = "Set up JDK",
            action = SetupJava(
                distribution = SetupJava.Distribution.Corretto,
                javaVersion = jdkVersion
            )
        )
        uses(name = "Setup Gradle", action = ActionsSetupGradle())

        run(
            name = "Grant permission to execute gradlew",
            command = "chmod +x gradlew"
        )

        run(
            name = "Build release APK and AAB",
            command = "./gradlew :androidApp:assembleRelease :androidApp:bundleRelease"
        )

        uses(
            name = "Upload APK",
            action = UploadArtifact(
                name = "android-apk",
                path = listOf("androidApp/build/outputs/apk/release/")
            )
        )
        uses(
            name = "Upload AAB",
            action = UploadArtifact(
                name = "android-aab",
                path = listOf("androidApp/build/outputs/bundle/release/")
            )
        )
        // configuration.txt records every keep rule R8 actually applied, including the consumer
        // rules coming from :composeApp, :os-map and :location-clients. Keep it: without it a
        // shrinking regression is nearly impossible to diagnose after the fact.
        uses(
            name = "Upload R8 mapping",
            action = UploadArtifact(
                name = "android-mapping",
                path = listOf("androidApp/build/outputs/mapping/release/")
            )
        )
    }

    job(
        id = "desktop",
        runsOn = RunnerType.Custom(expr("matrix.os")),
        needs = listOf(test),
        _customArguments = mapOf(
            "strategy" to mapOf(
                "fail-fast" to false,
                "matrix" to mapOf(
                    "os" to listOf("macos-latest", "windows-latest", "ubuntu-latest")
                )
            )
        )
    ) {
        uses(name = "Checkout code", action = Checkout())
        uses(
            name = "Set up JDK",
            action = SetupJava(
                distribution = SetupJava.Distribution.Corretto,
                javaVersion = jdkVersion
            )
        )
        uses(name = "Setup Gradle", action = ActionsSetupGradle())

        // bash on every runner: on the Windows image the default shell is PowerShell, which cannot
        // execute the `gradlew` shell script.
        run(
            name = "Grant permission to execute gradlew",
            command = "chmod +x gradlew",
            shell = Shell.Bash
        )

        run(
            name = "Build native distribution",
            command = "./gradlew :composeApp:packageReleaseDistributionForCurrentOS",
            shell = Shell.Bash
        )

        uses(
            name = "Upload distribution",
            action = UploadArtifact(
                name = "desktop-${expr("matrix.os")}",
                path = listOf("composeApp/build/compose/binaries/main-release/")
            )
        )
    }
}
