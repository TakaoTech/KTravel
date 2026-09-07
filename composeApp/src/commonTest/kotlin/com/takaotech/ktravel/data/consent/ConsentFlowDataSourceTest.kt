package com.takaotech.ktravel.data.consent

import com.takaotech.ktravel.presentation.plan.day.isValidMarkdown
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.allDrawableResources

/**
 * The privacy notice as it is actually shipped.
 *
 * Against the packaged files rather than a fixture, because everything worth checking is a property
 * of the content: that the Italian translation did not fall behind the English original, that every
 * illustration it names still exists, and that a body which is Markdown parses as Markdown.
 */
class ConsentFlowDataSourceTest :
    BehaviorSpec({
        val dataSource = ConsentFlowDataSource()

        given("the shipped notice") {
            `when`("it is read in English") {
                then("its cards are Markdown and exactly one of them asks the question") {
                    val flow = dataSource.load(DEFAULT_CONSENT_LANGUAGE)

                    flow.language shouldBe DEFAULT_CONSENT_LANGUAGE
                    flow.introCards.isNotEmpty() shouldBe true
                    flow.introCards.count { it.isDecision } shouldBe 1
                    flow.introCards.forEach { card ->
                        card.title.isNotBlank() shouldBe true
                        isValidMarkdown(card.message) shouldBe true
                    }
                }
            }

            `when`("it is read in Italian") {
                then("it is the same notice: same version, same cards, same order") {
                    val english = dataSource.load(DEFAULT_CONSENT_LANGUAGE)
                    val italian = dataSource.load("it")

                    italian.version shouldBe english.version
                    italian.introCards.map { it.id } shouldBe english.introCards.map { it.id }
                    italian.introCards.map { it.media } shouldBe english.introCards.map { it.media }
                    italian.introCards.map { it.isDecision } shouldBe english.introCards.map { it.isDecision }
                }
            }

            `when`("it is read in a language nobody translated") {
                then("the English one is used") {
                    dataSource.load("xx").language shouldBe DEFAULT_CONSENT_LANGUAGE
                }
            }

            `when`("its illustrations are resolved") {
                then("every drawable it names is packaged with the application") {
                    val named = dataSource.load(DEFAULT_CONSENT_LANGUAGE).introCards
                        .mapNotNull { it.media }
                        .filterIsInstance<com.takaotech.ktravel.domain.staticflows.ConsentMedia.Static>()
                        .map { it.name }

                    named.forEach { name -> (Res.allDrawableResources[name] != null) shouldBe true }
                }
            }
        }
    })
