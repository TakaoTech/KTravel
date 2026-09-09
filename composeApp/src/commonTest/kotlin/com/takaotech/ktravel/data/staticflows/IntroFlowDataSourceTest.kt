package com.takaotech.ktravel.data.staticflows

import com.takaotech.ktravel.domain.staticflows.IntroFlow
import com.takaotech.ktravel.domain.staticflows.IntroMedia
import com.takaotech.ktravel.domain.staticflows.IntroStep
import com.takaotech.ktravel.domain.staticflows.paths
import com.takaotech.ktravel.presentation.plan.day.isValidMarkdown
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.allDrawableResources

/**
 * The introduction as it is actually shipped.
 *
 * Against the packaged files rather than a fixture, because everything worth checking is a property
 * of the content: that the Italian translation did not fall behind the English original, that every
 * illustration it names still exists, that a body which is Markdown parses as Markdown, and that
 * every point it makes about privacy still names a section of the policy that was written.
 */
class IntroFlowDataSourceTest :
    BehaviorSpec({
        val dataSource = IntroFlowDataSource()
        val policySource = PrivacyPolicyDataSource()

        given("the shipped introduction") {
            `when`("it is read in English") {
                then("its steps are Markdown and exactly one of them asks the question") {
                    val flow = dataSource.load(DEFAULT_CONTENT_LANGUAGE)

                    flow.language shouldBe DEFAULT_CONTENT_LANGUAGE
                    flow.steps.isNotEmpty() shouldBe true
                    flow.steps.count { it is IntroStep.Decision } shouldBe 1
                    flow.steps.forEach { step ->
                        step.title.isNotBlank() shouldBe true

                        when (step) {
                            is IntroStep.Card -> isValidMarkdown(step.body) shouldBe true

                            is IntroStep.Decision -> isValidMarkdown(step.body) shouldBe true

                            is IntroStep.Privacy -> {
                                step.details.isNotEmpty() shouldBe true
                                step.details.forEach { isValidMarkdown(it.body) shouldBe true }
                            }
                        }
                    }
                }
            }

            `when`("it is read in Italian") {
                then("it is the same introduction: same version, same steps, same order") {
                    val english = dataSource.load(DEFAULT_CONTENT_LANGUAGE)
                    val italian = dataSource.load("it")

                    italian.version shouldBe english.version
                    italian.steps.map { it.id } shouldBe english.steps.map { it.id }
                    italian.steps.map { it::class } shouldBe english.steps.map { it::class }
                    italian.privacyDetailIds() shouldBe english.privacyDetailIds()
                }
            }

            `when`("it is read in a language nobody translated") {
                then("the English one is used") {
                    dataSource.load("xx").language shouldBe DEFAULT_CONTENT_LANGUAGE
                }
            }

            `when`("its illustrations are resolved") {
                then("every drawable it names is packaged with the application") {
                    val named = dataSource.load(DEFAULT_CONTENT_LANGUAGE).steps
                        .mapNotNull {
                            when (it) {
                                is IntroStep.Card -> it.media
                                is IntroStep.Decision -> it.media
                                is IntroStep.Privacy -> null
                            }
                        }
                        .filterIsInstance<IntroMedia.Static>()
                        .map { it.name }

                    named.isNotEmpty() shouldBe true
                    named.forEach { name -> (Res.allDrawableResources[name] != null) shouldBe true }
                }
            }

            `when`("what it says about privacy is resolved against the policy") {
                then("every path it names is a section somebody wrote") {
                    val paths = policySource.load(DEFAULT_CONTENT_LANGUAGE).paths()
                    val referenced = dataSource.load(DEFAULT_CONTENT_LANGUAGE).steps.flatMap { step ->
                        when (step) {
                            is IntroStep.Privacy -> step.details.map { it.policyRef }
                            is IntroStep.Decision -> listOfNotNull(step.policyRef)
                            is IntroStep.Card -> emptyList()
                        }
                    }

                    referenced.isNotEmpty() shouldBe true
                    referenced.forEach { paths.contains(it) shouldBe true }
                }
            }
        }
    })

/** The ids of every privacy point, in order, which is what has to match across languages. */
private fun IntroFlow.privacyDetailIds(): List<String> =
    steps.filterIsInstance<IntroStep.Privacy>().flatMap { step -> step.details.map { it.id } }
