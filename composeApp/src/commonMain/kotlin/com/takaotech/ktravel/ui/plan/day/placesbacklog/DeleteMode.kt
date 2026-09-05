package com.takaotech.ktravel.ui.plan.day.placesbacklog

import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.place_delete
import ktravel.composeapp.generated.resources.place_delete_permanent
import org.jetbrains.compose.resources.StringResource

/**
 * How a place leaves the backlog.
 *
 * @property text Label of the entry in the delete menu.
 */
enum class DeleteMode(val text: StringResource) {
    /** Out of this trip: the place stays in the library and can be added back. */
    GENERAL(Res.string.place_delete),

    /** Out of the library for good, behind a confirmation. */
    PERMANENT(Res.string.place_delete_permanent),
}
