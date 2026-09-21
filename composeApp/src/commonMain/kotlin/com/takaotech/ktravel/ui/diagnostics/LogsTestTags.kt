package com.takaotech.ktravel.ui.diagnostics

/** Test tags of the diagnostics screen. */
internal object LogsTestTags {
    const val LIST = "diagnostics_list"
    const val EMPTY = "diagnostics_empty"
    const val SEARCH = "diagnostics_search"
    const val DAY_FILTER = "diagnostics_day_filter"
    const val LEVEL_FILTER = "diagnostics_level_filter"
    const val CONSENT_SWITCH = "diagnostics_consent_switch"
    const val FORGET_ME = "diagnostics_forget_me"
    const val RETENTION = "diagnostics_retention"
    const val SAVE = "diagnostics_save"
    const val REPORT = "diagnostics_report"
    const val CLEAR = "diagnostics_clear"

    /** The row of the line with [id], so a test can name the one it means. */
    fun row(id: String): String = "diagnostics_row_$id"
}
