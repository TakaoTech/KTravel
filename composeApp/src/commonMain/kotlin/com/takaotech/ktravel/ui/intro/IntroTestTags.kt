package com.takaotech.ktravel.ui.intro

/** Test tags of the introduction. */
internal object IntroTestTags {
    const val PAGER = "intro_pager"
    const val NEXT = "intro_next"
    const val BACK = "intro_back"
    const val ALLOW = "intro_allow"
    const val DENY = "intro_deny"
    const val LOADING = "intro_loading"
    const val COUNTER = "intro_step_counter"
    const val ACKNOWLEDGE = "intro_privacy_acknowledge"
    const val ACKNOWLEDGE_LINK = "intro_privacy_acknowledge_link"
    const val FINISH = "intro_finish"

    /** The step with [id], so a test can name the one it means. */
    fun step(id: String): String = "intro_step_$id"

    /** The privacy point with [id], the row that opens the document on its section. */
    fun detail(id: String): String = "intro_detail_$id"
}
