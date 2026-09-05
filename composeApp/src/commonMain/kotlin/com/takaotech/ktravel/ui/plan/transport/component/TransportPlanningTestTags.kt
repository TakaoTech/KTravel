package com.takaotech.ktravel.ui.plan.transport.component

/**
 * Test tags for the transport screen; the host tests resolve strings in Italian, so nodes are found
 * by tag.
 */
internal object TransportPlanningTestTags {
    const val NAVIGATOR_EMBEDDED = "transport_navigator_embedded"
    const val NAVIGATOR_REMOTE = "transport_navigator_remote"
    const val REACHABILITY = "transport_reachability"
    const val RETRY_CATALOG = "transport_retry_catalog"
    const val CALCULATE = "transport_calculate"
    const val TIME_NOW = "transport_time_now"
    const val TIME_NOW_INFO = "transport_time_now_info"
    const val TIME_NOW_TOOLTIP = "transport_time_now_tooltip"
    const val TIME_DEPART_AT = "transport_time_depart_at"
    const val TIME_ARRIVE_BY = "transport_time_arrive_by"
    const val TIME_VALUE = "transport_time_value"
    const val SHORTEST = "transport_shortest"
    const val ALTERNATIVES = "transport_alternatives"
    const val CHANGES_LIMIT = "transport_changes_limit"
    const val CHANGES_SLIDER = "transport_changes_slider"
    const val WALK_DISTANCE_LIMIT = "transport_walk_distance_limit"
    const val WALK_DISTANCE_SLIDER = "transport_walk_distance_slider"
    const val FAILURE = "transport_failure"

    fun profileTag(provider: String, profile: String): String = "transport_profile_${provider}_$profile"

    fun modeTag(modeId: String): String = "transport_mode_$modeId"

    fun avoidTag(featureName: String): String = "transport_avoid_$featureName"

    fun walkingPaceTag(paceName: String): String = "transport_walking_pace_$paceName"
}
