package com.takaotech.ktravel.ui.privacy

/** Test tags of the privacy policy document. */
internal object PrivacyPolicyTestTags {
    const val LIST = "privacy_policy_list"
    const val LOADING = "privacy_policy_loading"
    const val INDEX = "privacy_policy_index"
    const val INDEX_TOGGLE = "privacy_policy_index_toggle"

    /** The section at [path], so a test can name the one it means. */
    fun section(path: String): String = "privacy_policy_section_$path"

    /** The index entry that jumps to [path]. */
    fun indexEntry(path: String): String = "privacy_policy_index_entry_$path"
}
