package com.takaotech.ktravel.ui.consent

/** Test tags of the privacy notice. */
internal object ConsentTestTags {
    const val PAGER = "consent_pager"
    const val NEXT = "consent_next"
    const val ALLOW = "consent_allow"
    const val DENY = "consent_deny"
    const val LOADING = "consent_loading"

    /** The card with [id], so a test can name the one it means. */
    fun card(id: String): String = "consent_card_$id"
}
