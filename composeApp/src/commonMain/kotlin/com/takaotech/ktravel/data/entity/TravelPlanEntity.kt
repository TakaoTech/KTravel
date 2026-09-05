package com.takaotech.ktravel.data.entity

import com.takaotech.ktravel.core.data.mime.MimeType
import com.takaotech.ktravel.data.entity.TravelPlanEntity.Companion.DOCUMENT_TYPE
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.time.Instant

/**
 * A travel plan as it is stored: one document of the `travel_plans` collection, and the unit an
 * `.ktravel` archive carries.
 *
 * Every field is written under an explicit `@SerialName`, so the stored keys survive a rename of
 * the Kotlin property. Renaming a key is the opposite — a schema change that needs a migration, see
 * `TravelPlanJsonMigration`. Readers ignore keys they do not know and write defaults out, so a
 * field added by a newer build costs an older one nothing but the value it never learns about.
 *
 * @property id Identity of the document holding the plan, filled in from the store's metadata when
 * it is read and supplied by the caller when it is written. Transient: it is the document's own
 * identity, not part of its body.
 * @property type Discriminator every document of the collection carries, always [DOCUMENT_TYPE].
 * The query listing the plans filters on it.
 * @property name Title the traveller gave the plan.
 * @property periodStart First day the plan covers.
 * @property periodEnd Last day the plan covers.
 * @property days The itinerary, one entry per day of the trip.
 * @property places The plan's backlog: places to fit in somewhere, not yet in any day.
 * @property settings Preferences set on this plan. Defaulted, so a document written before they
 * existed still decodes.
 */
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
        } + places.flatMap { it.attachments } + days.flatMap { day -> day.places }
        .flatMap { it.attachments }

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

    /** Constants of the stored form. */
    companion object {
        /** Value of `type` on every document holding a travel plan. */
        const val DOCUMENT_TYPE = "travel_plan"
    }
}

/**
 * Preferences the user sets on a single travel plan. Every field has a default, so a document
 * written before a preference existed stays readable.
 *
 * @property hereApiKey HERE key this plan computes with, empty when none is set. The exporter
 * always clears it: the key travels only inside `secrets.json`, encrypted.
 * @property navigatorPreference Which navigator the transport screen of this plan starts on.
 * A string rather than a serialized enum so that a document written by a newer build, naming a
 * kind this one has never heard of, still decodes: an unknown value falls back to the embedded
 * server instead of failing the whole plan.
 * @property navigatorRemoteBaseUrl Navigator this plan uses instead of the one configured for the
 * installation, empty when it uses that one.
 */
@Serializable
data class TravelSettingsEntity(
    @SerialName("here_api_key") val hereApiKey: String = "",
    @SerialName("navigator_preference") val navigatorPreference: String = "",
    @SerialName("navigator_remote_base_url") val navigatorRemoteBaseUrl: String = "",
)

/**
 * One day of the trip: what is planned for it, and what is still waiting to be placed in it.
 *
 * @property id Identity of the day inside the plan.
 * @property date The day itself. The key reads `date_epoch_days`, but the value is a date in ISO
 * 8601 form and not a count of days; the key stays as it is because changing it would make every
 * stored document unreadable. The one field that really holds a day count is
 * [VisitScheduleEntity.dateEpochDays].
 * @property steps The itinerary of the day, in travel order.
 * @property places The day's own backlog: places meant for this day, not yet in its itinerary.
 */
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
 *
 * @property id Identity of the place inside the plan.
 * @property name What the place is called.
 * @property lat Latitude in degrees.
 * @property lng Longitude in degrees.
 * @property note Free-form Markdown the traveller wrote about the place.
 * @property attachments Files the traveller attached to the place.
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

/**
 * When a place of the itinerary is visited: a day, and the hours the visit runs between.
 *
 * Every part is optional and stored on its own, so a visit can name only a day, or a day and the
 * hour it starts at. An hour without its minute is not a time: such a pair reads back as no time at
 * all rather than as an hour on the dot.
 *
 * @property dateEpochDays Day of the visit, in days since the epoch.
 * @property startTimeHour Hour the visit starts at, from 0 to 23.
 * @property startTimeMinute Minute the visit starts at, from 0 to 59.
 * @property endTimeHour Hour the visit ends at, from 0 to 23.
 * @property endTimeMinute Minute the visit ends at, from 0 to 59.
 */
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
 *
 * @property id Identity of the attachment inside the plan.
 * @property relativePath Where the file sits under the attachments root.
 * @property originalName Name the file had when it was picked, which is the name the traveller is
 * shown.
 * @property mimeType What kind of file it is, which is what decides whether it is previewed as an
 * image.
 * @property sizeBytes Size of the file in bytes.
 */
@Serializable
data class AttachmentEntity(
    @SerialName("id") val id: String,
    @SerialName("relative_path") val relativePath: String,
    @SerialName("original_name") val originalName: String,
    @SerialName("mime_type") val mimeType: MimeType,
    @SerialName("size_bytes") val sizeBytes: Long,
)

/**
 * One entry of a day's itinerary: a place to be at, or the transport that gets the traveller to the
 * next one.
 *
 * Sealed and written with a `type` discriminator (`place`, `transport`), so a step is read back as
 * the kind it was written as.
 *
 * @property id Identity of the step inside the plan.
 */
@Serializable
sealed class StepEntity {
    abstract val id: String

    /**
     * A place placed in the itinerary, and the time it is visited at.
     *
     * @property name What the place is called. Written under the key `location`, which is the key
     * the documents already saved use.
     * @property lat Latitude in degrees.
     * @property lng Longitude in degrees.
     * @property schedule When the visit happens, absent while it has not been scheduled.
     * @property note Free-form Markdown the traveller wrote about the step.
     * @property attachments Files the traveller attached to the step. Empty by default, so the
     * documents saved before the inventory existed still decode.
     */
    @Serializable
    @SerialName("place")
    data class Place(
        override val id: String,
        @SerialName("location") val name: String,
        @SerialName("lat") val lat: Double,
        @SerialName("lng") val lng: Double,
        @SerialName("schedule") val schedule: VisitScheduleEntity? = null,
        @SerialName("note") val note: String = "",
        @SerialName("attachments") val attachments: List<AttachmentEntity> = emptyList(),
    ) : StepEntity()

    /**
     * The transport that joins two places of the itinerary, and what was computed for it.
     *
     * @property transportType Which vehicle the traveller takes, as the name of the domain enum.
     * @property answer What the calculation answered, in the shape it answered it. Required: a
     * transport that has not been computed is not written, so there is no such thing as one without
     * an answer.
     * @property request The request the answer was chosen from. Absent from the documents written
     * before the request was recorded, hence the default.
     * @property note Free-form Markdown the traveller wrote about the step.
     * @property attachments Files the traveller attached to the step. Empty by default, so the
     * documents saved before the inventory existed still decode.
     * @property calculatedAt When the route was computed, null for the transports filed before it
     * was recorded.
     */
    @Serializable
    @SerialName("transport")
    data class Transport(
        override val id: String,
        @SerialName("transport_type") val transportType: String,
        @SerialName("answer") val answer: TransportAnswerEntity,
        @SerialName("request") val request: TransportRequestEntity? = null,
        @SerialName("note") val note: String = "",
        @SerialName("attachments") val attachments: List<AttachmentEntity> = emptyList(),
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

    /**
     * A route on roads, travelled by one vehicle.
     *
     * @property mode The vehicle the route was computed for.
     * @property avoid What the route was asked to keep away from, as the names of the domain
     * features. Names this build does not know are dropped when it is read.
     * @property shortestDistance Whether the shortest route was asked for rather than the fastest.
     */
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

    /**
     * A journey on scheduled services.
     *
     * @property modeFilter The kinds of service the journey was restricted to, empty when it was
     * not restricted.
     * @property maxChanges How many changes were allowed, null when no limit was asked for.
     * @property pedestrianSpeedMetersPerSecond Walking speed the connections were computed with, in
     * meters per second.
     * @property pedestrianMaxDistanceMeters How far the traveller was willing to walk, in meters.
     */
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

    /**
     * A route on roads.
     *
     * @property route The road route that was computed.
     */
    @Serializable
    @SerialName("routing")
    data class Routing(@SerialName("route") val route: RoutingRouteEntity) : TransportAnswerEntity()

    /**
     * A journey on scheduled services.
     *
     * @property journey The journey that was computed.
     */
    @Serializable
    @SerialName("transit")
    data class Transit(@SerialName("journey") val journey: TransitJourneyEntity) : TransportAnswerEntity()
}

/**
 * Totals of a leg or of one of its parts.
 *
 * @property durationSeconds How long it takes, in seconds.
 * @property distanceMeters How far it goes, in meters. Plain numbers in a fixed unit, so the stored
 * form depends on no unit type.
 */
@Serializable
data class RouteSummaryEntity(
    @SerialName("duration_seconds") val durationSeconds: Long,
    @SerialName("distance_meters") val distanceMeters: Double,
)

/**
 * A point on the ground.
 *
 * @property lat Latitude in degrees.
 * @property lng Longitude in degrees.
 */
@Serializable
data class RouteLocationEntity(@SerialName("lat") val lat: Double, @SerialName("lng") val lng: Double)

/**
 * A moment on a timetable.
 *
 * ISO 8601 **with the offset in force where it happens**, which is the offset at the stop and not
 * the one the device is in: a journey is read off a departure board, so dropping it would move every
 * time of a trip planned abroad.
 *
 * @property location Where it happens.
 * @property time When it happens, null when the profile did not answer with one.
 */
@Serializable
data class RouteDepartureEntity(
    @SerialName("location") val location: RouteLocationEntity,
    @SerialName("time") val time: String? = null,
)

/**
 * A road route: the totals, and the legs between waypoints.
 *
 * @property summary Totals of the whole route.
 * @property sections The legs, in travel order.
 */
@Serializable
data class RoutingRouteEntity(
    @SerialName("summary") val summary: RouteSummaryEntity,
    @SerialName("sections") val sections: List<RoutingSectionEntity>,
)

/**
 * One leg of a road route, with the manoeuvres to perform along it.
 *
 * @property summary Totals of the leg.
 * @property mode The vehicle it is covered by.
 * @property actions The manoeuvres along it, in travel order.
 * @property departure Where and when the leg starts, when the profile answered with it.
 * @property arrival Where and when the leg ends, when the profile answered with it.
 * @property polyline The shape of the leg, encoded, which is what the map draws.
 */
@Serializable
data class RoutingSectionEntity(
    @SerialName("summary") val summary: RouteSummaryEntity,
    @SerialName("mode") val mode: String,
    @SerialName("actions") val actions: List<RouteActionEntity> = emptyList(),
    @SerialName("departure") val departure: RouteDepartureEntity? = null,
    @SerialName("arrival") val arrival: RouteDepartureEntity? = null,
    @SerialName("polyline") val polyline: String? = null,
)

/**
 * A journey on scheduled services: the totals, and the walking and riding in travel order.
 *
 * @property summary Totals of the whole journey.
 * @property steps The walking and the rides, in travel order.
 */
@Serializable
data class TransitJourneyEntity(
    @SerialName("summary") val summary: RouteSummaryEntity,
    @SerialName("steps") val steps: List<TransitStepEntity>,
)

/**
 * One step of a journey: either the traveller walks it, or a scheduled vehicle carries them.
 *
 * @property summary Totals of the step.
 * @property polyline The shape of the step, encoded, which is what the map draws.
 */
@Serializable
sealed class TransitStepEntity {
    abstract val summary: RouteSummaryEntity
    abstract val polyline: String?

    /**
     * A stretch covered on foot.
     *
     * @property departureTime When the traveller sets off, ISO 8601 with the offset in force there.
     * @property arrivalTime When the traveller gets there, ISO 8601 with the offset in force there.
     * @property from Where the walking starts.
     * @property to Where the walking ends.
     */
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

    /**
     * A stretch aboard a scheduled service.
     *
     * @property line The service that carries the traveller.
     * @property agency Who runs it, when the profile named them.
     * @property boarding The stop the traveller gets on at.
     * @property alighting The stop the traveller gets off at.
     * @property intermediateStops The stops called at in between, in travel order.
     */
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

/**
 * The service operating a ride, as it is written on the vehicle.
 *
 * Everything but the mode is optional, because it is what an operator publishes rather than
 * something the plan can be sure of.
 *
 * @property mode The kind of service, a tram and a night bus being read differently by a traveller.
 * @property name The line as the operator names it.
 * @property shortName What is written on the front of the vehicle, when it is shorter than [name].
 * @property longName The line's full name, when the operator publishes one.
 * @property category The class of service, which is what tells a regional train from a high speed
 * one.
 * @property headsign Where the vehicle is signed for, which is what the traveller checks on the
 * platform.
 * @property color Colour of the line, as the operator publishes it.
 * @property textColor Colour to write on top of [color], as the operator publishes it.
 * @property url Where the operator describes the line.
 * @property wheelchairAccessible How well the service serves a traveller in a wheelchair, as the
 * name of the domain value. Unknown wording, and no wording at all, both read as unknown and never
 * as a no.
 */
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

/**
 * Who runs a service, which is who to ask about a disruption.
 *
 * @property name The operator's name.
 * @property id The operator's identifier in the profile that answered.
 * @property website Where the operator publishes their own information.
 */
@Serializable
data class TransitAgencyEntity(
    @SerialName("name") val name: String,
    @SerialName("id") val id: String? = null,
    @SerialName("website") val website: String? = null,
)

/**
 * A stop on a journey: where the traveller boards, alights, or passes through.
 *
 * @property location Where the stop is.
 * @property name What the stop is called.
 * @property arrivalTime When the vehicle gets there, ISO 8601 with the offset in force at the stop.
 * @property departureTime When it leaves, ISO 8601 with the offset in force at the stop.
 * @property dwellSeconds How long it stands there, in seconds.
 * @property offset Index into the ride's polyline, which is what puts a marker on the map without
 * looking the stop up again.
 * @property url Where the operator describes the stop.
 * @property wheelchairAccessible How well the stop serves a traveller in a wheelchair, as the name
 * of the domain value. Unknown wording, and no wording at all, both read as unknown and never as a
 * no.
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
 *
 * @property action What the traveller does, as the profile names it.
 * @property durationSeconds How long it takes, in seconds.
 * @property distanceMeters How far it runs, in meters.
 * @property instruction The manoeuvre written out, in the language it was asked for.
 * @property direction Which way it turns, when the profile answered with it.
 * @property severity How sharp it is, when the profile answered with it.
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
