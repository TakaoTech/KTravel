@file:OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)

package com.takaotech.ktravel.presentation.planning

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.text.input.TextFieldValue
import com.takaotech.ktravel.core.toLocalDate
import com.takaotech.ktravel.domain.archive.TravelArchiveError
import com.takaotech.ktravel.domain.model.TransportType
import com.takaotech.ktravel.domain.routing.model.Route
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.datetime.LocalDate
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

/** Avanzamento dell'export del viaggio verso un file scelto dall'utente. */
@Immutable
sealed interface ExportUiState {
    data object Idle : ExportUiState
    data object InProgress : ExportUiState

    /** [skippedAttachments] conta i file referenziati dal piano ma non più presenti su disco. */
    data class Completed(val skippedAttachments: Int) : ExportUiState
    data class Failed(val error: TravelArchiveError) : ExportUiState
}

@Stable
data class PlanHeader(
    val name: TextFieldValue = TextFieldValue(""),
    private val mPeriod: Period = Period()
) {
    val period: Period = mPeriod

    @Stable
    data class Period(
        val start: Long = Clock.System.now().toEpochMilliseconds(),
        val end: Long = start
    )
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
        /** Note libere in formato Markdown associate allo step. */
        val note: String = "",
        /** Inventario file dello step. */
        val attachments: PersistentList<AttachmentUi> = persistentListOf(),
    ) : StepUi(id)

    @Stable
    data class Transport(
        override val id: String = Uuid.random().toString(),
        val type: TransportType,
        val route: Route,
        /** Durata complessiva della tratta, aggregata dalle sezioni del [route]. */
        val totalDuration: Duration = route.sections.fold(Duration.ZERO) { acc, section ->
            acc + section.summary.durationSeconds
        },
    ) : StepUi(id)
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
data class PlaceUi(
    val id: String = Uuid.random().toString(),
    val name: String,
    val lat: Double,
    val lng: Double
)
