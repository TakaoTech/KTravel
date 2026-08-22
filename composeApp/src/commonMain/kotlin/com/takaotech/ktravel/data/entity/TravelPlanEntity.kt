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
    @SerialName("places") val places: List<PlaceEntity>,
    @SerialName("settings") val settings: TravelSettingsEntity = TravelSettingsEntity(),
) {
    companion object {
        const val DOCUMENT_TYPE = "travel_plan"
    }
}

/**
 * Preferences the user sets on a single travel plan. Every field has a default, so a document
 * written before a preference existed stays readable.
 *
 * @constructor Creates a new TravelSettingsEntity
 * @property hereApiKey the here api key
 * @property navigatorPreference Which navigator the transport screen of this plan starts on.
 * A string rather than a serialized enum so that a document written by a newer build, naming a
 * kind this one has never heard of, still decodes: an unknown value falls back to the embedded
 * server instead of failing the whole plan.
 * @property navigatorRemoteBaseUrl the navigator remote base url
 */
@Serializable
data class TravelSettingsEntity(
    // The exporter always clears this field: the key travels only inside `secrets.json`, encrypted.
    @SerialName("here_api_key") val hereApiKey: String = "",
    @SerialName("navigator_preference") val navigatorPreference: String = "",
    @SerialName("navigator_remote_base_url") val navigatorRemoteBaseUrl: String = "",
)

@Serializable
data class TravelDayEntity(
    @SerialName("id") val id: String,
    @SerialName("date_epoch_days") val date: LocalDate,
    @SerialName("steps") val steps: List<StepEntity>,
    @SerialName("places") val places: List<PlaceEntity>,
)

@Serializable
data class PlaceEntity(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("lat") val lat: Double,
    @SerialName("lng") val lng: Double,
)

@Serializable
data class VisitScheduleEntity(
    @SerialName("date_epoch_days") val dateEpochDays: Int? = null,
    @SerialName("start_time_hour") val startTimeHour: Int? = null,
    @SerialName("start_time_minute") val startTimeMinute: Int? = null,
    @SerialName("end_time_hour") val endTimeHour: Int? = null,
    @SerialName("end_time_minute") val endTimeMinute: Int? = null,
)

/**
 * Metadata of a file in a step's inventory. The binary lives on disk
 * (`<travelId>/<stepId>/<uuid>.<ext>`); only the [relativePath] relative to the attachments root is
 * kept here, never an absolute path (on iOS the sandbox container changes between launches).
 */
@Serializable
data class AttachmentEntity(
    @SerialName("id") val id: String,
    @SerialName("relative_path") val relativePath: String,
    @SerialName("original_name") val originalName: String,
    @SerialName("mime_type") val mimeType: String,
    @SerialName("size_bytes") val sizeBytes: Long,
)

@Serializable
sealed class StepEntity {
    abstract val id: String

    @Serializable
    @SerialName("place")
    data class Place(
        override val id: String,
        // SerialName "location" kept for backward compatibility with the documents already saved.
        @SerialName("location") val name: String,
        @SerialName("lat") val lat: Double,
        @SerialName("lng") val lng: Double,
        @SerialName("schedule") val schedule: VisitScheduleEntity? = null,
        @SerialName("note") val note: String = "",
        // File inventory of the step. Empty default = backward compatible with the saved documents.
        @SerialName("attachments") val attachments: List<AttachmentEntity> = emptyList(),
    ) : StepEntity()

    @Serializable
    @SerialName("transport")
    data class Transport(
        override val id: String,
        @SerialName("transport_type") val transportType: String,
        @SerialName("route") val route: RouteEntity,
        // Absent from the documents written before the request was recorded, hence the default.
        @SerialName("request") val request: TransportRequestEntity? = null,
    ) : StepEntity()
}

/**
 * The request that produced the alternatives a transport was chosen from.
 *
 * Sealed for the same reason `RouteSelection` is upstream: the two families do not take the same
 * parameters, and one flat record would carry "avoid tolls" into a train journey and "how many
 * transfers" into a car route.
 *
 * Vehicles and profiles cross as plain strings because they are plain identifiers on the way in
 * too: their vocabulary belongs to the profile that declares them and is not knowable here.
 *
 * @property provider Who computes the route.
 * @property profile Which of that provider's APIs does.
 * @property alternatives How many routes were asked for.
 */
@Serializable
sealed class TransportRequestEntity {
    abstract val provider: String
    abstract val profile: String
    abstract val alternatives: Int

    /** A route on roads, travelled by one vehicle. */
    @Serializable
    @SerialName("routing")
    data class Routing(
        @SerialName("provider") override val provider: String,
        @SerialName("profile") override val profile: String,
        @SerialName("alternatives") override val alternatives: Int = 1,
        @SerialName("mode") val mode: String,
        @SerialName("avoid") val avoid: List<String> = emptyList(),
        @SerialName("shortest_distance") val shortestDistance: Boolean = false,
    ) : TransportRequestEntity()

    /** A journey on scheduled services. */
    @Serializable
    @SerialName("transit")
    data class Transit(
        @SerialName("provider") override val provider: String,
        @SerialName("profile") override val profile: String,
        @SerialName("alternatives") override val alternatives: Int = 1,
        @SerialName("mode_filter") val modeFilter: List<String> = emptyList(),
        @SerialName("max_changes") val maxChanges: Int? = null,
        @SerialName("pedestrian_speed_mps") val pedestrianSpeedMetersPerSecond: Double? = null,
        @SerialName("pedestrian_max_distance_meters") val pedestrianMaxDistanceMeters: Int? = null,
    ) : TransportRequestEntity()
}

@Serializable
data class RouteEntity(@SerialName("sections") val sections: List<RouteSectionEntity>)

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
    @SerialName("actions") val actions: List<RouteActionEntity> = emptyList(),
)

@Serializable
data class RouteActionEntity(
    @SerialName("action") val action: String,
    @SerialName("duration_seconds") val durationSeconds: Long,
    @SerialName("distance_meters") val distanceMeters: Double,
    @SerialName("instruction") val instruction: String? = null,
    @SerialName("direction") val direction: String? = null,
    @SerialName("severity") val severity: String? = null,
)
