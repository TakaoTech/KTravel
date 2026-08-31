package com.takaotech.ktravel.data.entity

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.time.Instant

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
    /**
     * Every attachment the plan references, in order of appearance.
     *
     * Lives here rather than with the archive that reads it: knowing where files hang off a plan is
     * knowing the shape of a plan. The backlog counts as much as the itinerary — a place carries its
     * files in and out of the days — so a new home for an attachment is added in this one place.
     */
    fun allAttachments(): List<AttachmentEntity> = days.flatMap { day -> day.steps }
        .flatMap { step ->
            when (step) {
                is StepEntity.Place -> step.attachments
                is StepEntity.Transport -> step.attachments
            }
        } + places.flatMap { it.attachments } + days.flatMap { day -> day.places }.flatMap { it.attachments }

    /**
     * Copy of the plan whose inventory holds only [retained], the counterpart of [allAttachments]:
     * what that one finds, this one can drop. References inside the notes are left untouched — they
     * stay dangling exactly as they already were.
     */
    fun retainingOnly(retained: List<AttachmentEntity>): TravelPlanEntity {
        val keep = retained.map { it.relativePath }.toSet()
        fun List<PlaceEntity>.retained(): List<PlaceEntity> = map { place ->
            place.copy(attachments = place.attachments.filter { it.relativePath in keep })
        }
        return copy(
            days = days.map { day ->
                day.copy(
                    steps = day.steps.map { step ->
                        when (step) {
                            is StepEntity.Transport -> step.copy(
                                attachments = step.attachments.filter { it.relativePath in keep },
                            )

                            is StepEntity.Place -> step.copy(
                                attachments = step.attachments.filter { it.relativePath in keep },
                            )
                        }
                    },
                    places = day.places.retained(),
                )
            },
            places = places.retained(),
        )
    }

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

/**
 * Place waiting to enter the itinerary. Carries the same note and file inventory a place step does:
 * moving one in or out of the itinerary must not cost the traveller what they wrote and attached.
 */
@Serializable
data class PlaceEntity(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("lat") val lat: Double,
    @SerialName("lng") val lng: Double,
    @SerialName("note") val note: String,
    @SerialName("attachments") val attachments: List<AttachmentEntity>,
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
        // What the calculation answered, in the shape it answered it. Required: a transport that
        // has not been computed is not written, so there is no such thing as one without an answer.
        @SerialName("answer") val answer: TransportAnswerEntity,
        // Absent from the documents written before the request was recorded, hence the default.
        @SerialName("request") val request: TransportRequestEntity? = null,
        @SerialName("note") val note: String = "",
        // File inventory of the step. Empty default = backward compatible with the saved documents.
        @SerialName("attachments") val attachments: List<AttachmentEntity> = emptyList(),
        // When the route was computed. Null for the transports filed before it was recorded.
        @SerialName("calculated_at") val calculatedAt: Instant? = null,
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

/**
 * What a calculation answered, told apart by kind on the way in and on the way out.
 *
 * Sealed for the same reason [TransportRequestEntity] is: the two profiles do not answer the same
 * thing. A road route is a shape with manoeuvres along it; a journey is a sequence of departures to
 * be at on time, run by operators, calling at stops. One record for both meant every journey was
 * saved as the handful of fields the two have in common, and read back without the rest.
 */
@Serializable
sealed class TransportAnswerEntity {

    /** A route on roads. */
    @Serializable
    @SerialName("routing")
    data class Routing(@SerialName("route") val route: RoutingRouteEntity) : TransportAnswerEntity()

    /** A journey on scheduled services. */
    @Serializable
    @SerialName("transit")
    data class Transit(@SerialName("journey") val journey: TransitJourneyEntity) : TransportAnswerEntity()
}

/** Totals of a leg or of one of its parts. */
@Serializable
data class RouteSummaryEntity(
    @SerialName("duration_seconds") val durationSeconds: Long,
    @SerialName("distance_meters") val distanceMeters: Double,
)

/** A point on the ground. */
@Serializable
data class RouteLocationEntity(@SerialName("lat") val lat: Double, @SerialName("lng") val lng: Double)

/**
 * A moment on a timetable.
 *
 * ISO 8601 **with the offset in force where it happens**, which is the offset at the stop and not
 * the one the device is in: a journey is read off a departure board, so dropping it would move every
 * time of a trip planned abroad.
 */
@Serializable
data class RouteDepartureEntity(
    @SerialName("location") val location: RouteLocationEntity,
    @SerialName("time") val time: String? = null,
)

/** A road route: the totals, and the legs between waypoints. */
@Serializable
data class RoutingRouteEntity(
    @SerialName("summary") val summary: RouteSummaryEntity,
    @SerialName("sections") val sections: List<RoutingSectionEntity>,
)

/** One leg of a road route, with the manoeuvres to perform along it. */
@Serializable
data class RoutingSectionEntity(
    @SerialName("summary") val summary: RouteSummaryEntity,
    @SerialName("mode") val mode: String,
    @SerialName("actions") val actions: List<RouteActionEntity> = emptyList(),
    @SerialName("departure") val departure: RouteDepartureEntity? = null,
    @SerialName("arrival") val arrival: RouteDepartureEntity? = null,
    @SerialName("polyline") val polyline: String? = null,
)

/** A journey on scheduled services: the totals, and the walking and riding in travel order. */
@Serializable
data class TransitJourneyEntity(
    @SerialName("summary") val summary: RouteSummaryEntity,
    @SerialName("steps") val steps: List<TransitStepEntity>,
)

/** One step of a journey: either the traveller walks it, or a scheduled vehicle carries them. */
@Serializable
sealed class TransitStepEntity {
    abstract val summary: RouteSummaryEntity
    abstract val polyline: String?

    /** A stretch covered on foot. */
    @Serializable
    @SerialName("walk")
    data class Walk(
        @SerialName("summary") override val summary: RouteSummaryEntity,
        @SerialName("polyline") override val polyline: String? = null,
        @SerialName("departure_time") val departureTime: String? = null,
        @SerialName("arrival_time") val arrivalTime: String? = null,
        @SerialName("from") val from: RouteLocationEntity? = null,
        @SerialName("to") val to: RouteLocationEntity? = null,
    ) : TransitStepEntity()

    /** A stretch aboard a scheduled service. */
    @Serializable
    @SerialName("ride")
    data class Ride(
        @SerialName("summary") override val summary: RouteSummaryEntity,
        @SerialName("line") val line: TransitLineEntity,
        @SerialName("polyline") override val polyline: String? = null,
        @SerialName("agency") val agency: TransitAgencyEntity? = null,
        @SerialName("boarding") val boarding: TransitStopEntity? = null,
        @SerialName("alighting") val alighting: TransitStopEntity? = null,
        @SerialName("intermediate_stops") val intermediateStops: List<TransitStopEntity> = emptyList(),
    ) : TransitStepEntity()
}

/** The service operating a ride, as it is written on the vehicle. */
@Serializable
data class TransitLineEntity(
    @SerialName("mode") val mode: String,
    @SerialName("name") val name: String? = null,
    @SerialName("short_name") val shortName: String? = null,
    @SerialName("long_name") val longName: String? = null,
    @SerialName("category") val category: String? = null,
    @SerialName("headsign") val headsign: String? = null,
    @SerialName("color") val color: String? = null,
    @SerialName("text_color") val textColor: String? = null,
    @SerialName("url") val url: String? = null,
    @SerialName("wheelchair") val wheelchairAccessible: String? = null,
)

/** Who runs a service, which is who to ask about a disruption. */
@Serializable
data class TransitAgencyEntity(
    @SerialName("name") val name: String,
    @SerialName("id") val id: String? = null,
    @SerialName("website") val website: String? = null,
)

/**
 * A stop on a journey: where the traveller boards, alights, or passes through.
 *
 * @property offset Index into the ride's polyline, which is what puts a marker on the map without
 * looking the stop up again.
 */
@Serializable
data class TransitStopEntity(
    @SerialName("location") val location: RouteLocationEntity,
    @SerialName("name") val name: String? = null,
    @SerialName("arrival_time") val arrivalTime: String? = null,
    @SerialName("departure_time") val departureTime: String? = null,
    @SerialName("dwell_seconds") val dwellSeconds: Long? = null,
    @SerialName("offset") val offset: Int? = null,
    @SerialName("url") val url: String? = null,
    @SerialName("wheelchair") val wheelchairAccessible: String? = null,
)

/**
 * One manoeuvre.
 *
 * [offset] is the index into the section's polyline where it happens: without it, tapping the
 * manoeuvre of a route read back from the plan has nowhere to move the camera to.
 */
@Serializable
data class RouteActionEntity(
    @SerialName("action") val action: String,
    @SerialName("duration_seconds") val durationSeconds: Long,
    @SerialName("distance_meters") val distanceMeters: Double,
    @SerialName("instruction") val instruction: String? = null,
    @SerialName("offset") val offset: Int? = null,
    @SerialName("direction") val direction: String? = null,
    @SerialName("severity") val severity: String? = null,
)
