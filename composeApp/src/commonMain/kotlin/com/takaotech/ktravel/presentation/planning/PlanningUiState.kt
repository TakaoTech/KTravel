@file:OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)

package com.takaotech.ktravel.presentation.planning

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.text.input.TextFieldValue
import com.takaotech.ktravel.core.toLocalDate
import com.takaotech.ktravel.domain.archive.TravelArchiveError
import com.takaotech.ktravel.domain.model.TransportType
import com.takaotech.ktravel.domain.routing.model.TransportAnswer
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Stable
data class PlanningUiState(
    val planHeader: PlanHeader = PlanHeader(),
    val days: PersistentList<TravelDayUi> = persistentListOf(),
    val places: PersistentList<PlaceUi> = persistentListOf(),
    val export: ExportUiState = ExportUiState.Idle,
) {
    fun setPeriod(start: Instant, end: Instant): PlanningUiState =
        (start.toLocalDate()..end.toLocalDate()).map { newDate ->
            days.firstOrNull { it.date == newDate } ?: TravelDayUi(date = newDate)
        }.let {
            copy(
                planHeader = planHeader.copy(
                    mPeriod = PlanHeader.Period(
                        start = start.toEpochMilliseconds(),
                        end = end.toEpochMilliseconds(),
                    ),
                ),
                days = it.toPersistentList(),
            )
        }
}

/** Progress of the trip export towards a file the user picked. */
@Immutable
sealed interface ExportUiState {
    data object Idle : ExportUiState

    /**
     * The plan has an API key, so the user is being asked whether to include it and under which
     * password. Reached before the file saver: there is no point picking a destination for an
     * export the user may still cancel.
     */
    data object AwaitingSecretsChoice : ExportUiState

    data object InProgress : ExportUiState

    /** [skippedAttachments] counts the files the plan references but that are no longer on disk. */
    data class Completed(val skippedAttachments: Int) : ExportUiState
    data class Failed(val error: TravelArchiveError) : ExportUiState
}

@Stable
data class PlanHeader(val name: TextFieldValue = TextFieldValue(""), private val mPeriod: Period = Period()) {
    val period: Period = mPeriod

    @Stable
    data class Period(val start: Long = Clock.System.now().toEpochMilliseconds(), val end: Long = start)
}

@Stable
data class TravelDayUi(
    val id: String = Uuid.random().toString(),
    val date: LocalDate,
    val steps: PersistentList<StepUi> = persistentListOf(),
    val places: PersistentList<PlaceUi> = persistentListOf(),
) {

    // TODO Review this variable place
    /** Place stops of the day: transport steps are only shown in the day detail. */
    val placeSteps: PersistentList<StepUi.Place> =
        steps.filterIsInstance<StepUi.Place>().toPersistentList()

    companion object {
        val EMPTY = TravelDayUi(
            id = "",
            date = LocalDate.fromEpochDays(0),
        )
    }
}

@Stable
sealed class StepUi(open val id: String = Uuid.random().toString()) {
    @Stable
    data class Place(
        override val id: String = Uuid.random().toString(),
        val name: String,
        val lat: Double,
        val lng: Double,
        val schedule: VisitScheduleUi? = null,
        /** Free-form Markdown notes attached to the step. */
        val note: String = "",
        /** File inventory of the step. */
        val attachments: PersistentList<AttachmentUi> = persistentListOf(),
    ) : StepUi(id)

    @Stable
    data class Transport(
        override val id: String = Uuid.random().toString(),
        val type: TransportType,
        /** What the calculation answered: a road route, or a journey on scheduled services. */
        val answer: TransportAnswer,
        /**
         * How long the leg takes.
         *
         * Read off the answer rather than summed here: for a journey the total that matters is door
         * to door, waits on the platform included, and those belong to no step to be summed.
         */
        val totalDuration: Duration = answer.summary.durationSeconds,
        /** Free-form Markdown notes attached to the leg. */
        val note: String = "",
        /** File inventory of the leg. */
        val attachments: PersistentList<AttachmentUi> = persistentListOf(),
        /** When the route was computed, null when the plan does not record it. */
        val calculatedAt: Instant? = null,
    ) : StepUi(id) {

        /**
         * When the leg leaves, as the saved answer times it.
         *
         * Derived rather than stored, the way [totalDuration] is: the answer is what carries the
         * timetable, and a copy of it here would be one more thing to keep in step with it. Null
         * when the calculation produced no times at all, which is what a route asked for "now" does.
         */
        val departure: LocalDateTime? get() = answer.departureTime

        /** When it lands, with the same rule as [departure]. */
        val arrival: LocalDateTime? get() = answer.arrivalTime
    }
}

@Stable
data class VisitScheduleUi(
    val date: LocalDate? = null,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
)

@Stable
data class AttachmentUi(
    val id: String,
    val relativePath: String,
    val originalName: String,
    val mimeType: String,
    val isImage: Boolean,
)

@Stable
data class PlaceUi(val id: String = Uuid.random().toString(), val name: String, val lat: Double, val lng: Double)
