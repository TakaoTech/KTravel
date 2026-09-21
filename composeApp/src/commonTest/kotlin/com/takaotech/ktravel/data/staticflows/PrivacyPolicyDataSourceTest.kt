package com.takaotech.ktravel.data.staticflows

import com.takaotech.ktravel.domain.staticflows.PrivacyPolicy
import com.takaotech.ktravel.domain.staticflows.PrivacyPolicySection
import com.takaotech.ktravel.domain.staticflows.paths
import com.takaotech.ktravel.domain.staticflows.section
import com.takaotech.ktravel.presentation.plan.day.isValidMarkdown
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

/**
 * The privacy policy as it is actually shipped.
 *
 * The paths are the contract here: the introduction points at them, the document scrolls to them,
 * and a translation that renames one silently breaks both. So they are compared across languages,
 * and they are checked for duplicates — two sections sharing a path would make one unreachable.
 */
class PrivacyPolicyDataSourceTest :
    BehaviorSpec({
        val dataSource = PrivacyPolicyDataSource()

        given("the shipped policy") {
            `when`("it is read in English") {
                then("every section has a title, a Markdown body and a path of its own") {
                    val policy = dataSource.load(DEFAULT_CONTENT_LANGUAGE)
                    val paths = policy.paths()

                    policy.language shouldBe DEFAULT_CONTENT_LANGUAGE
                    policy.sections.isNotEmpty() shouldBe true
                    paths.distinct().size shouldBe paths.size
                    policy.walk().forEach { section ->
                        section.title.isNotBlank() shouldBe true
                        isValidMarkdown(section.body) shouldBe true
                    }
                }
            }

            `when`("every path it lists is looked up") {
                then("each one resolves to the section it was built from") {
                    val policy = dataSource.load(DEFAULT_CONTENT_LANGUAGE)

                    policy.paths().forEach { path ->
                        policy.section(path)?.id shouldBe path.substringAfterLast('.')
                    }
                }
            }

            `when`("it is read in Italian") {
                then("it is the same policy: same version, same sections, same paths") {
                    val english = dataSource.load(DEFAULT_CONTENT_LANGUAGE)
                    val italian = dataSource.load("it")

                    italian.version shouldBe english.version
                    italian.paths() shouldBe english.paths()
                }
            }

            `when`("it is read in a language nobody translated") {
                then("the English one is used") {
                    dataSource.load("xx").language shouldBe DEFAULT_CONTENT_LANGUAGE
                }
            }
        }
    })

/** Every section of the policy, parents before their children. */
private fun PrivacyPolicy.walk(): List<PrivacyPolicySection> = sections.flatMap { it.walk() }

private fun PrivacyPolicySection.walk(): List<PrivacyPolicySection> = listOf(this) + subsections.flatMap { it.walk() }
