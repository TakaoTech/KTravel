package com.takaotech.ktravel.data.entity

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class TravelPlanEntity(
    @Transient val id: String = "",
    @SerialName("type") val type: String = DOCUMENT_TYPE,
    @SerialName("name") val name: String,
    @SerialName("period_start") val periodStart: LocalDate,
    @SerialName("period_end") val periodEnd: LocalDate,
    @SerialName("days") val days: List<TravelDayEntity>,
    @SerialName("places") val places: List<PlaceEntity>
) {
    companion object {
        const val DOCUMENT_TYPE = "travel_plan"
    }
}

@Serializable
data class TravelDayEntity(
    @SerialName("id") val id: String,
    @SerialName("date_epoch_days") val date: LocalDate,
    @SerialName("steps") val steps: List<StepEntity>,
    @SerialName("places") val places: List<PlaceEntity>
)

@Serializable
data class PlaceEntity(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("lat") val lat: Double,
    @SerialName("lng") val lng: Double,
    @SerialName("schedule") val schedule: VisitScheduleEntity? = null
)

@Serializable
data class VisitScheduleEntity(
    @SerialName("date_epoch_days") val dateEpochDays: Int? = null,
    @SerialName("time_hour") val timeHour: Int,
    @SerialName("time_minute") val timeMinute: Int
)

@Serializable
sealed class StepEntity {
    abstract val id: String

    @Serializable
    @SerialName("place")
    data class Place(
        override val id: String,
        @SerialName("location") val location: String,
        @SerialName("lat") val lat: Double,
        @SerialName("lng") val lng: Double
    ) : StepEntity()

    @Serializable
    @SerialName("transport")
    data class Transport(
        override val id: String,
        @SerialName("transport_type") val transportType: String,
        @SerialName("route") val route: RouteEntity
    ) : StepEntity()
}

@Serializable
data class RouteEntity(
    @SerialName("sections") val sections: List<RouteSectionEntity>
)

@Serializable
data class RouteSectionEntity(
    @SerialName("duration_seconds") val durationSeconds: Long,
    @SerialName("distance_meters") val distanceMeters: Double,
    @SerialName("polyline") val polyline: String? = null,
    @SerialName("transport_mode") val transportMode: String? = null,
    @SerialName("departure_lat") val departureLat: Double? = null,
    @SerialName("departure_lng") val departureLng: Double? = null,
    @SerialName("arrival_lat") val arrivalLat: Double? = null,
    @SerialName("arrival_lng") val arrivalLng: Double? = null,
    @SerialName("actions") val actions: List<RouteActionEntity> = emptyList()
)

@Serializable
data class RouteActionEntity(
    @SerialName("action") val action: String,
    @SerialName("duration_seconds") val durationSeconds: Long,
    @SerialName("distance_meters") val distanceMeters: Double,
    @SerialName("instruction") val instruction: String? = null,
    @SerialName("direction") val direction: String? = null,
    @SerialName("severity") val severity: String? = null
)
