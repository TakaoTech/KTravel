#!/usr/bin/env kotlin

@file:Suppress("MaxLineLength")

@file:Repository("https://repo.maven.apache.org/maven2/")
@file:DependsOn("io.github.typesafegithub:github-workflows-kt:3.7.0")
@file:Repository("https://bindings.krzeminski.it")

@file:DependsOn("actions:checkout:v4")
@file:DependsOn("actions:setup-java:v4")
@file:DependsOn("gradle:actions__setup-gradle:v4")
@file:DependsOn("github:codeql-action__upload-sarif:v3")


import io.github.typesafegithub.workflows.actions.actions.Checkout
import io.github.typesafegithub.workflows.actions.actions.SetupJava
import io.github.typesafegithub.workflows.actions.github.CodeqlActionUploadSarif
import io.github.typesafegithub.workflows.actions.gradle.ActionsSetupGradle
import io.github.typesafegithub.workflows.domain.RunnerType
import io.github.typesafegithub.workflows.domain.triggers.Push
import io.github.typesafegithub.workflows.dsl.workflow

workflow(
    name = "Detekt",
    on = listOf(
        Push(branches = listOf("main", "dev")),
    ),
    sourceFile = __FILE__
) {
    job(id = "detekt", runsOn = RunnerType.UbuntuLatest) {
        uses(name = "Checkout code", action = Checkout())
        uses(
            name = "Set up JDK",
            action = SetupJava(
                distribution = SetupJava.Distribution.Corretto,
                javaVersion = "17"
            ),
            _customArguments = mapOf(

            )
        )

        uses(
            name = "Setup Gradle",
            action = ActionsSetupGradle(),
        )

        run(
            name = "Grant permission to execute gradlew",
            command = "chmod +x gradlew"
        )

        run(name = "Run detektAll", command = "./gradlew detektAll")
        uses(
            name = "Upload SARIF file",
            action = CodeqlActionUploadSarif(
                sarifFile = "build/reports/detekt/detekt.sarif"
            )
        )
    }
}
