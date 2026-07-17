@file:OptIn(ExperimentalUuidApi::class)

package com.takaotech.ktravel.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private fun newId(): String = Uuid.random().toString()

data class TravelPlanSummary(
    val id: String,
    val name: String,
    val periodStart: LocalDate,
    val periodEnd: LocalDate
)

data class TravelPlanDomain(
    val id: String = newId(),
    val name: String = "",
    val periodStart: LocalDate = LocalDate.fromEpochDays(0),
    val periodEnd: LocalDate = LocalDate.fromEpochDays(0),
    val days: List<TravelDayDomain> = emptyList(),
    val places: List<PlaceDomain> = emptyList()
)

data class TravelDayDomain(
    val id: String = newId(),
    val date: LocalDate,
    val steps: List<StepDomain> = emptyList(),
    val places: List<PlaceDomain> = emptyList()
) {
    companion object {
        val EMPTY = TravelDayDomain(
            id = "",
            date = LocalDate.fromEpochDays(0)
        )
    }
}

data class PlaceDomain(
    val id: String = newId(),
    val name: String,
    val lat: Double,
    val lng: Double
)

data class VisitScheduleDomain(
    val date: LocalDate? = null,
    val time: LocalTime
)

/**
 * File dell'inventario di uno step. [relativePath] è relativo alla root degli allegati
 * (`<travelId>/<stepId>/<uuid>.<ext>`); [isImage] deriva dal [mimeType].
 */
data class AttachmentDomain(
    val id: String = newId(),
    val relativePath: String,
    val originalName: String,
    val mimeType: String,
    val sizeBytes: Long
) {
    val isImage: Boolean get() = mimeType.startsWith("image/")
}

sealed class StepDomain(open val id: String = newId()) {
    /**
     * Step di un luogo collocato nell'itinerario.
     *
     * A differenza di [PlaceDomain] (backlog senza tempo), lo step è l'unico titolare
     * dell'orario di visita ([schedule], vincolo V2.1).
     */
    data class Place(
        override val id: String = newId(),
        val name: String,
        val lat: Double,
        val lng: Double,
        val schedule: VisitScheduleDomain? = null,
        /** Note libere in formato Markdown associate allo step. */
        val note: String = "",
        /** Inventario file dello step (foto e documenti). */
        val attachments: List<AttachmentDomain> = emptyList()
    ) : StepDomain(id)

    data class Transport(
        override val id: String = newId(),
        val type: TransportType,
        val route: com.takaotech.ktravel.domain.routing.model.Route
    ) : StepDomain(id)
}

enum class TransportType { TRAIN, BUS, CAR, FLIGHT }
