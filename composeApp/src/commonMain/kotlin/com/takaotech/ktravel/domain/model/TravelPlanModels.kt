@file:OptIn(ExperimentalUuidApi::class)

package com.takaotech.ktravel.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private fun newId(): String = Uuid.random().toString()

data class TravelPlanSummary(val id: String, val name: String, val periodStart: LocalDate, val periodEnd: LocalDate)

data class TravelPlanDomain(
    val id: String = newId(),
    val name: String = "",
    val periodStart: LocalDate = LocalDate.fromEpochDays(0),
    val periodEnd: LocalDate = LocalDate.fromEpochDays(0),
    val days: List<TravelDayDomain> = emptyList(),
    val places: List<PlaceDomain> = emptyList(),
    /**
     * Preferences the user set on this plan.
     *
     * They have to live in the domain model, not only in the entity: every mutation goes through
     * `TravelPlanRepositoryImpl.persistCurrentState`, which rebuilds the entity from this state, so
     * a field missing here would be wiped on the next edit of the plan.
     */
    val settings: TravelSettingsDomain = TravelSettingsDomain(),
)

/**
 * Preferences of a single travel plan, read and written through
 * `com.takaotech.ktravel.domain.repository.SettingsRepository`.
 */
data class TravelSettingsDomain(
    /** HERE API key of this plan, empty when none is configured. */
    val hereApiKey: String = "",
)

data class TravelDayDomain(
    val id: String = newId(),
    val date: LocalDate,
    val steps: List<StepDomain> = emptyList(),
    val places: List<PlaceDomain> = emptyList(),
) {
    companion object {
        val EMPTY = TravelDayDomain(
            id = "",
            date = LocalDate.fromEpochDays(0),
        )
    }
}

data class PlaceDomain(val id: String = newId(), val name: String, val lat: Double, val lng: Double)

data class VisitScheduleDomain(
    val date: LocalDate? = null,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
)

/**
 * File in a step's inventory. [relativePath] is relative to the attachments root
 * (`<travelId>/<stepId>/<uuid>.<ext>`); [isImage] is derived from the [mimeType].
 */
data class AttachmentDomain(
    val id: String = newId(),
    val relativePath: String,
    val originalName: String,
    val mimeType: String,
    val sizeBytes: Long,
) {
    val isImage: Boolean get() = mimeType.startsWith("image/")
}

sealed class StepDomain(open val id: String = newId()) {
    /**
     * Step of a place placed in the itinerary.
     *
     * Unlike [PlaceDomain] (a backlog with no time), the step is the sole owner of the visit time
     * ([schedule], constraint V2.1).
     */
    data class Place(
        override val id: String = newId(),
        val name: String,
        val lat: Double,
        val lng: Double,
        val schedule: VisitScheduleDomain? = null,
        /** Free-form Markdown notes attached to the step. */
        val note: String = "",
        /** File inventory of the step (photos and documents). */
        val attachments: List<AttachmentDomain> = emptyList(),
    ) : StepDomain(id)

    data class Transport(
        override val id: String = newId(),
        val type: TransportType,
        val route: com.takaotech.ktravel.domain.routing.model.Route,
    ) : StepDomain(id)
}

enum class TransportType { TRAIN, BUS, CAR, FLIGHT }
