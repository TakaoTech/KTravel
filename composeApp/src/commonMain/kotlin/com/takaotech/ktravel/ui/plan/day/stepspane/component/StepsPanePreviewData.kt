package com.takaotech.ktravel.ui.plan.day.stepspane.component

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.takaotech.ktravel.presentation.plan.VisitScheduleUi
import kotlinx.datetime.LocalTime

/**
 * Every combination of arrival and departure the two chips are asked to draw.
 *
 * The half filled pairs are the reason this provider exists rather than a single fixture: a chip
 * that is set sits next to one that is not, and the filled against dashed contrast is the whole
 * signal separating a planned stop from an unplanned one. Both halves are kept because the chips
 * are not interchangeable — arrival is drawn first, departure second — and a preview of only the
 * first would never show a dashed chip in second position.
 */
internal class ScheduleChipsPreviewParams : PreviewParameterProvider<VisitScheduleUi?> {
    override val values = sequenceOf(
        null,
        VisitScheduleUi(startTime = ARRIVAL),
        VisitScheduleUi(endTime = DEPARTURE),
        VisitScheduleUi(startTime = ARRIVAL, endTime = DEPARTURE),
    )
}

private val ARRIVAL = LocalTime(hour = 9, minute = 30)

private val DEPARTURE = LocalTime(hour = 11, minute = 0)
