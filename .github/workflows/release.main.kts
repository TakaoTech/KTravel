#!/usr/bin/env kotlin

@file:Suppress("MaxLineLength")

@file:Repository("https://repo.maven.apache.org/maven2/")
@file:DependsOn("io.github.typesafegithub:github-workflows-kt:3.7.0")
@file:Repository("https://bindings.krzeminski.it")

@file:DependsOn("actions:checkout:v7")
@file:DependsOn("actions:setup-java:v5")
@file:DependsOn("actions:upload-artifact:v7")
@file:DependsOn("actions:upload-pages-artifact:v5")
@file:DependsOn("actions:deploy-pages:v5")
@file:DependsOn("gradle:actions__setup-gradle:v6")

import io.github.typesafegithub.workflows.actions.actions.Checkout
import io.github.typesafegithub.workflows.actions.actions.DeployPages
import io.github.typesafegithub.workflows.actions.actions.SetupJava
import io.github.typesafegithub.workflows.actions.actions.UploadArtifact
import io.github.typesafegithub.workflows.actions.actions.UploadPagesArtifact
import io.github.typesafegithub.workflows.actions.gradle.ActionsSetupGradle
import io.github.typesafegithub.workflows.domain.Environment
import io.github.typesafegithub.workflows.domain.Mode
import io.github.typesafegithub.workflows.domain.Permission
import io.github.typesafegithub.workflows.domain.RunnerType
import io.github.typesafegithub.workflows.domain.Shell
import io.github.typesafegithub.workflows.domain.triggers.Push
import io.github.typesafegithub.workflows.domain.triggers.WorkflowDispatch
import io.github.typesafegithub.workflows.dsl.expressions.expr
import io.github.typesafegithub.workflows.dsl.workflow
import io.github.typesafegithub.workflows.yaml.ConsistencyCheckJobConfig

val jdkVersion = "25"

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

    val licenseEnv = mapOf("GITHUB_TOKEN" to expr("secrets.GITHUB_TOKEN"))
    val fetchRemoteLicenses = "-Pktravel.licenses.fetchRemote=true"

    job(id = "android", runsOn = RunnerType.UbuntuLatest, needs = listOf(test), env = licenseEnv) {
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
            command = "./gradlew :androidApp:assembleRelease :androidApp:bundleRelease $fetchRemoteLicenses"
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
        // rules coming from :composeApp and :gunzou-here-client. Keep it: without it a
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
        env = licenseEnv,
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
            command = "./gradlew :composeApp:packageReleaseDistributionForCurrentOS $fetchRemoteLicenses",
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

    // The API documentation ships with the release: what Pages serves then always describes the
    // version that was just built, never an intermediate state of the default branch.
    val docs = job(id = "docs", runsOn = RunnerType.UbuntuLatest, needs = listOf(test)) {
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

        // :dokkaAll aggregates the six documented modules into build/dokka/html. The runner is
        // Linux, where the Kotlin/Native targets are disabled, so iosMain is absent from the
        // output; everything the app exposes lives in commonMain, which is documented.
        run(
            name = "Generate API documentation",
            command = "./gradlew dokkaAll"
        )

        uses(
            name = "Upload Pages artifact",
            action = UploadPagesArtifact(path = "build/dokka/html")
        )
    }

    // Split from the job above so the write permissions on Pages are held by the deployment alone.
    job(
        id = "deploy-docs",
        runsOn = RunnerType.UbuntuLatest,
        needs = listOf(docs),
        permissions = mapOf(
            Permission.Pages to Mode.Write,
            Permission.IdToken to Mode.Write
        ),
        // Pages deployments are gated on this environment; the URL is what the run summary links.
        environment = Environment(
            name = "github-pages",
            url = expr("steps.deployment.outputs.page_url")
        )
    ) {
        uses(name = "Deploy to GitHub Pages", action = DeployPages(), id = "deployment")
    }
}
