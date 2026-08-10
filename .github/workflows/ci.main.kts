#!/usr/bin/env kotlin

@file:Suppress("MaxLineLength")

@file:Repository("https://repo.maven.apache.org/maven2/")
@file:DependsOn("io.github.typesafegithub:github-workflows-kt:3.7.0")
@file:Repository("https://bindings.krzeminski.it")

@file:DependsOn("actions:checkout:v7")
@file:DependsOn("actions:setup-java:v5")
@file:DependsOn("actions:cache:v6")
@file:DependsOn("actions:upload-artifact:v7")
@file:DependsOn("gradle:actions__setup-gradle:v6")

import io.github.typesafegithub.workflows.actions.actions.Cache
import io.github.typesafegithub.workflows.actions.actions.Checkout
import io.github.typesafegithub.workflows.actions.actions.SetupJava
import io.github.typesafegithub.workflows.actions.actions.UploadArtifact
import io.github.typesafegithub.workflows.actions.gradle.ActionsSetupGradle
import io.github.typesafegithub.workflows.domain.Mode
import io.github.typesafegithub.workflows.domain.Permission
import io.github.typesafegithub.workflows.domain.RunnerType
import io.github.typesafegithub.workflows.domain.triggers.PullRequest
import io.github.typesafegithub.workflows.domain.triggers.Push
import io.github.typesafegithub.workflows.dsl.expressions.expr
import io.github.typesafegithub.workflows.dsl.workflow
import io.github.typesafegithub.workflows.yaml.ConsistencyCheckJobConfig

val jdkVersion = "25"

// On a pull_request event the checkout lands on the merge commit in detached HEAD, which has no
// branch to push back to. Checking out the head ref of the PR instead gives the format job
// somewhere to commit, and lets the verify job pick up that commit.
val checkoutRef = expr("github.event.pull_request.head.ref || github.ref")
val checkoutRepo = expr("github.event.pull_request.head.repo.full_name || github.repository")

// Pull requests opened from a fork get a read-only token: the push would fail. Those runs format
// and report, they just cannot commit the result back.
val isNotFork =
    "github.event_name != 'pull_request' || github.event.pull_request.head.repo.full_name == github.repository"

workflow(
    name = "CI",
    on = listOf(
        Push(branches = listOf("main", "dev")),
        PullRequest(),
    ),
    sourceFile = __FILE__,
    consistencyCheckJobConfig = ConsistencyCheckJobConfig.Disabled,
) {
    val format = job(
        id = "format",
        runsOn = RunnerType.UbuntuLatest,
        permissions = mapOf(Permission.Contents to Mode.Write),
    ) {
        uses(
            name = "Checkout code",
            action = Checkout(ref = checkoutRef, repository = checkoutRepo),
        )
        uses(
            name = "Set up JDK",
            action = SetupJava(
                distribution = SetupJava.Distribution.Corretto,
                javaVersion = jdkVersion,
            ),
        )
        uses(name = "Setup Gradle", action = ActionsSetupGradle())

        run(name = "Grant permission to execute gradlew", command = "chmod +x gradlew")

        // :detektFormat deliberately excludes the type-resolving detekt tasks: rewriting sources
        // while they hold a FIR model of the very same files makes them fail. See the root
        // build.gradle.kts for the details.
        run(
            name = "Apply detekt formatting",
            command = "./gradlew detektFormat -Pdetekt.autocorrect=true",
        )

        // The formatting commit carries [skip ci]: GitHub skips both the push and the
        // pull_request (synchronize) run it would otherwise trigger, so the pipeline does not
        // restart on its own output. The current run keeps going, and the verify job below checks
        // out the head ref, so the formatted commit is still the one that gets tested.
        run(
            name = "Commit formatting",
            condition = isNotFork,
            command = """
                if [ -z "${'$'}(git status --porcelain)" ]; then
                  echo "Nothing to format."
                  exit 0
                fi
                git config user.name "github-actions[bot]"
                git config user.email "41898282+github-actions[bot]@users.noreply.github.com"
                git add -A
                git commit -m "style: apply detekt/ktlint formatting [skip ci]"
                git push
            """.trimIndent(),
        )
    }

    job(
        id = "verify",
        runsOn = RunnerType.UbuntuLatest,
        needs = listOf(format),
        // Declared at job level on purpose: a step's own `env` is not visible to that step's `if`,
        // and the Sonar step is gated on the token being present.
        env = mapOf("SONAR_TOKEN" to expr("secrets.SONAR_TOKEN")),
    ) {
        uses(
            name = "Checkout code",
            // Full history: Sonar needs the blame to attribute issues to the New Code period.
            action = Checkout(
                ref = checkoutRef,
                repository = checkoutRepo,
                fetchDepth = Checkout.FetchDepth.Infinite
            ),
        )
        uses(
            name = "Set up JDK",
            action = SetupJava(
                distribution = SetupJava.Distribution.Corretto,
                javaVersion = jdkVersion,
            ),
        )

        uses(name = "Setup Gradle", action = ActionsSetupGradle())
        uses(
            name = "Cache SonarCloud packages",
            action = Cache(
                path = listOf("~/.sonar/cache"),
                key = "${expr("runner.os")}-sonar",
                restoreKeys = listOf("${expr("runner.os")}-sonar"),
            ),
        )

        run(name = "Grant permission to execute gradlew", command = "chmod +x gradlew")

        // jvmTest and testAndroidHostTest run the same commonTest suites on both JVM flavours.
        // Coverage comes from the JVM target, which is what Kover aggregates.
        // :androidApp:lint writes the Android Lint XML that Sonar reads; it does not fail the
        // build, because the module sets abortOnError = false.
        run(
            name = "Run tests and quality reports",
            command = "./gradlew jvmTest testAndroidHostTest koverXmlReport detektAll :androidApp:lint",
        )

        uses(
            name = "Upload test reports",
            condition = "always()",
            action = UploadArtifact(
                name = "test-reports",
                path = listOf("**/build/reports/tests/", "**/build/test-results/"),
                ifNoFilesFound = UploadArtifact.BehaviorIfNoFilesFound.Ignore,
            ),
        )

        // The scanner plugin is not configuration-cache compatible, hence the opt-out. Skipped
        // when the token is absent, which is the case for pull requests opened from a fork.
        // The pull request properties are passed explicitly: auto detection keys off the checked
        // out ref, and this job checks out the PR head rather than the merge commit.
        run(
            name = "SonarCloud analysis",
            condition = "env.SONAR_TOKEN != ''",
            command = """
                if [ "${expr("github.event_name")}" = "pull_request" ]; then
                  ./gradlew sonar --no-configuration-cache \
                    -Dsonar.pullrequest.key=${expr("github.event.pull_request.number")} \
                    -Dsonar.pullrequest.branch=${expr("github.head_ref")} \
                    -Dsonar.pullrequest.base=${expr("github.base_ref")}
                else
                  ./gradlew sonar --no-configuration-cache
                fi
            """.trimIndent(),
        )
    }
}
