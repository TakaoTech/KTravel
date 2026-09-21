package com.takaotech.ktravel.domain.staticflows

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The introduction to the application, as it is shipped: a versioned sequence of steps.
 *
 * Content rather than code, and content that has to be translated, so it lives in
 * `composeResources/files/intro/intro_flow_<language>.json` instead of in `strings.xml`. Two reasons
 * for the exception: the version has to travel with the text — an introduction is seen once, and a
 * newer one has to be seen again — and the steps are a list whose length is part of the content.
 *
 * Versioned apart from [PrivacyPolicy], which it only points at: rewording a welcome card must not
 * ask the user about telemetry again, and a new privacy policy must not replay the welcome.
 *
 * @property version The version of the introduction. Raising it shows it again.
 * @property language The language this file is written in, for diagnostics and tests.
 * @property steps What the user goes through, in order.
 */
@Serializable
data class IntroFlow(
    @SerialName("version")
    val version: Int,
    @SerialName("language")
    val language: String,
    @SerialName("steps")
    val steps: List<IntroStep>,
)

/**
 * One step of the introduction.
 *
 * Three shapes rather than one card with flags: what a step draws and what it asks are not the same
 * question, and a sealed hierarchy is what stops a card from carrying buttons it does not have.
 */
@Serializable
sealed interface IntroStep {

    /** Names the step. Stable across translations, which is what lets a test compare two languages. */
    val id: String

    /** The heading, in the language of the file. */
    val title: String

    /**
     * A page that is only read: what the application is, and what it does with what it is given.
     *
     * @property id Names the step.
     * @property title The heading.
     * @property body The text, in **Markdown**, rendered the way trip notes are.
     * @property media The illustration above the text, or none.
     */
    @Serializable
    @SerialName("card")
    data class Card(
        @SerialName("id") override val id: String,
        @SerialName("title") override val title: String,
        @SerialName("body") val body: String,
        @SerialName("media") val media: IntroMedia? = null,
    ) : IntroStep

    /**
     * The privacy page: what the policy asks for, said plainly.
     *
     * The legal text is not repeated here. Each [PrivacyDetail] is a sentence the reader can act on,
     * and the section it points at is what opens when they want the wording behind it.
     *
     * @property id Names the step.
     * @property title The heading.
     * @property body An optional line of introduction above the details, in **Markdown**.
     * @property details The points, in order.
     */
    @Serializable
    @SerialName("privacy")
    data class Privacy(
        @SerialName("id") override val id: String,
        @SerialName("title") override val title: String,
        @SerialName("body") val body: String? = null,
        @SerialName("details") val details: List<PrivacyDetail>,
    ) : IntroStep

    /**
     * The step that asks the question. Exactly one step is this one.
     *
     * @property id Names the step.
     * @property title The heading.
     * @property body The text, in **Markdown**.
     * @property media The illustration above the text, or none.
     * @property policyRef The path of the policy section that spells out what is being asked, or null.
     */
    @Serializable
    @SerialName("decision")
    data class Decision(
        @SerialName("id") override val id: String,
        @SerialName("title") override val title: String,
        @SerialName("body") val body: String,
        @SerialName("media") val media: IntroMedia? = null,
        @SerialName("policyRef") val policyRef: String? = null,
    ) : IntroStep
}

/**
 * One point of the privacy page: a summary the reader understands, and the section behind it.
 *
 * The summary is written here rather than taken from [PrivacyPolicy] because the two are written in
 * different registers — a sentence that reassures against a text that binds — and [policyRef] is
 * what keeps them tied together.
 *
 * @property id Names the point, stable across translations.
 * @property title The point itself, one line.
 * @property body What it means, in **Markdown**, kept to a sentence or two.
 * @property policyRef The path of the policy section it summarises, e.g. `collection.automatic`.
 */
@Serializable
data class PrivacyDetail(
    @SerialName("id")
    val id: String,
    @SerialName("title")
    val title: String,
    @SerialName("body")
    val body: String,
    @SerialName("policyRef")
    val policyRef: String,
)

/** The illustration of a step: an animation, or a drawable already in the application. */
@Serializable
sealed interface IntroMedia {

    /**
     * A Lottie animation packaged with the application.
     *
     * @property path Its resource path, e.g. `files/lottie_paper_airplane.json`.
     */
    @Serializable
    @SerialName("lottie")
    data class Lottie(val path: String) : IntroMedia

    /**
     * A drawable already packaged with the application.
     *
     * @property name Its resource name, without extension, e.g. `description`.
     */
    @Serializable
    @SerialName("drawable")
    data class Static(val name: String) : IntroMedia
}
