package com.takaotech.ktravel.ui.travels.list

internal object TravelListTestTags {
    const val SEARCH_BAR = "travel_selection_search_bar"
    const val FAB_NEW_TRAVEL = "travel_selection_fab"
    const val TOP_BAR_EXIT_SELECTION = "travel_selection_exit_selection"
    const val TOP_BAR_DELETE_SELECTED = "travel_selection_delete_selected"
    const val TOP_BAR_IMPORT = "travel_selection_import"
    const val TOP_BAR_APP_SETTINGS = "travel_selection_app_settings"
    fun travelItemTag(id: String) = "travel_item_$id"
}
