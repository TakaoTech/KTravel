package com.takaotech.ktravel.domain.staticflows

/**
 * How much of the introduction is due before the application can be used.
 *
 * Read from what the installation has already acknowledged — see
 * [com.takaotech.ktravel.domain.model.AppSettingsDomain.introRequirement] — and carried by the intro
 * screen, which is what says whether the reading cards are shown or skipped.
 */
enum class IntroRequirement {
    /** Nothing is due: the application opens on the trip list. */
    None,

    /** Everything is due: the introduction, the privacy page and the question. */
    Full,

    /**
     * Only the privacy page and the question.
     *
     * The user has already been introduced to the application; what changed is the policy, or the
     * year their answer was good for has run out.
     */
    PrivacyOnly,
}
