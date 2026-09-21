package com.takaotech.ktravel.ui.plan.settings

internal object TravelSettingsTestTags {
    const val API_KEY = "settings_api_key"
    const val TOGGLE_VISIBILITY = "settings_toggle_api_key_visibility"
    const val SAVE = "settings_save"
    const val NAVIGATOR_BASE_URL = "settings_navigator_base_url"
    const val NAVIGATOR_TEST = "settings_navigator_test"
    const val NAVIGATOR_REACHABILITY = "settings_navigator_reachability"
    const val DIAGNOSTICS = "settings_diagnostics"

    fun navigatorPreferenceTag(kind: String): String = "settings_navigator_preference_$kind"
}
