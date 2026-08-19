package com.takaotech.navigator.api.response

import com.takaotech.navigator.api.common.GeoPoint
import com.takaotech.navigator.api.common.ProviderId
import com.takaotech.navigator.api.common.ProviderProfile
import com.takaotech.navigator.api.common.TransitMode
import com.takaotech.navigator.api.common.ZonedTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The answer of a public transport profile, whichever engine computed it.
 *
 * Separate from [RoutingRouteResponse], and for the same reason the requests were separate from the
 * start: the two describe different things. A road route is a shape with manoeuvres along it and
 * possibly a toll; a journey is a sequence of departures the traveller has to be at on time, run by
 * operators, calling at stops. Carrying both in one type meant every road leg dragging an empty set
 * of stops and every journey leg dragging an empty set of tolls, and a reader unable to tell a field
 * that is off from one that does not apply.
 *
 * @property provider The engine that computed these journeys.
 * @property profile The profile of that engine, that is which of its APIs was called.
 * @property journeys The alternatives, best first. Empty is not an answer: a provider that finds
 *   nothing produces [com.takaotech.navigator.api.error.ErrorCode.NO_ROUTE_FOUND] instead.
 * @property notices Advisories about the whole answer, such as a request for more changes than the
 *   timetable allows. Never a substitute for an error.
 */
@Serializable
data class TransitJourneyResponse(
    @SerialName("provider") val provider: ProviderId,
    @SerialName("profile") val profile: ProviderProfile,
    @SerialName("journeys") val journeys: List<TransitJourneyDto> = emptyList(),
    @SerialName("notices") val notices: List<NoticeDto> = emptyList(),
)

/**
 * One way of getting there on scheduled services.
 *
 * @property summary Totals for the whole journey, aggregated by the server over [legs]. It is the
 *   time and distance actually travelled: the wait on a platform between two legs is not in it, so
 *   a screen showing how long the traveller will be out should use the departure and arrival times
 *   instead.
 * @property legs Walking and riding, alternating, in travel order.
 * @property notices Advisories about this alternative in particular, such as a connection that is
 *   tighter than the operator guarantees.
 */
@Serializable
data class TransitJourneyDto(
    @SerialName("summary") val summary: RouteSummaryDto,
    @SerialName("legs") val legs: List<TransitJourneyLeg> = emptyList(),
    @SerialName("notices") val notices: List<NoticeDto> = emptyList(),
)

/**
 * One leg of a journey: either the traveller walks it, or a scheduled vehicle carries them.
 *
 * A sealed type and not one record with optional fields, which is what the upstream API itself does
 * — HERE discriminates its sections on `type` and gives the two branches different members. The
 * gain is that a client's `when` is exhaustive: a third kind of leg, the day a provider has one,
 * does not compile until every screen has been told what to draw for it.
 *
 * [departureTime] and [arrivalTime] are declared here and computed by each variant so a caller can
 * read the shape of a journey without branching; they are not part of the encoded form.
 */
@Serializable
sealed interface TransitJourneyLeg {

    /** Totals for this leg. */
    val summary: RouteSummaryDto

    /** The drawable shape of this leg. */
    val geometry: RouteGeometry?

    /** When the traveller sets off on this leg. */
    val departureTime: ZonedTime?

    /** When the traveller gets to the end of it. */
    val arrivalTime: ZonedTime?

    /**
     * A leg covered on foot: to the first stop, between two of them, or off the last one.
     *
     * @property departure Where and when the walk starts.
     * @property arrival Where and when it ends.
     * @property actions The manoeuvres, when the request asked for them. Usually absent: a provider
     *   asked for a journey rarely returns turn by turn for the walking parts of it.
     */
    @Serializable
    @SerialName("walk")
    data class Walk(
        @SerialName("summary") override val summary: RouteSummaryDto,
        @SerialName("geometry") override val geometry: RouteGeometry? = null,
        @SerialName("departure") val departure: RouteWaypointDto? = null,
        @SerialName("arrival") val arrival: RouteWaypointDto? = null,
        @SerialName("actions") val actions: List<RouteActionDto> = emptyList(),
    ) : TransitJourneyLeg {
        override val departureTime: ZonedTime? get() = departure?.time
        override val arrivalTime: ZonedTime? get() = arrival?.time
    }

    /**
     * A leg aboard a scheduled service.
     *
     * @property line The service as the traveller sees it on the vehicle.
     * @property agency Who runs it, which is who to ask about a disruption.
     * @property boarding The stop the traveller gets on at.
     * @property alighting The stop they get off at.
     * @property intermediateStops What the vehicle calls at in between, when the request asked for
     *   them. Empty means they were not requested, not that the vehicle runs non stop.
     */
    @Serializable
    @SerialName("ride")
    data class Ride(
        @SerialName("summary") override val summary: RouteSummaryDto,
        @SerialName("line") val line: TransitLineDto,
        @SerialName("geometry") override val geometry: RouteGeometry? = null,
        @SerialName("agency") val agency: TransitAgencyDto? = null,
        @SerialName("boarding") val boarding: TransitStopDto? = null,
        @SerialName("alighting") val alighting: TransitStopDto? = null,
        @SerialName("intermediateStops") val intermediateStops: List<TransitStopDto> = emptyList(),
    ) : TransitJourneyLeg {
        override val departureTime: ZonedTime? get() = boarding?.departure ?: boarding?.arrival
        override val arrivalTime: ZonedTime? get() = alighting?.arrival ?: alighting?.departure
    }
}

/**
 * The service operating a [TransitJourneyLeg.Ride].
 *
 * @property mode The kind of vehicle.
 * @property name The line as it is written on the vehicle, such as `M1` or `62`.
 * @property shortName What fits on a badge, when the operator publishes a shorter form.
 * @property longName The full name of the line, usually its two termini.
 * @property category The operator's own wording for [mode], such as `Metro` or `Regional train`,
 *   which is often what the signage says and is worth showing over a translated enum.
 * @property headsign The destination shown on the front of the vehicle, which is how a traveller
 *   tells apart the two directions of the same line.
 * @property color Line colour as a `#RRGGBB` string, when the agency publishes one.
 * @property textColor Colour to draw on top of [color], for the same reason.
 * @property url Where the operator publishes the state of this line.
 * @property wheelchairAccessible Whether the vehicle can carry a wheelchair.
 */
@Serializable
data class TransitLineDto(
    @SerialName("mode") val mode: TransitMode,
    @SerialName("name") val name: String? = null,
    @SerialName("shortName") val shortName: String? = null,
    @SerialName("longName") val longName: String? = null,
    @SerialName("category") val category: String? = null,
    @SerialName("headsign") val headsign: String? = null,
    @SerialName("color") val color: String? = null,
    @SerialName("textColor") val textColor: String? = null,
    @SerialName("url") val url: String? = null,
    @SerialName("wheelchairAccessible") val wheelchairAccessible: WheelchairAccess = WheelchairAccess.UNKNOWN,
)

/**
 * The operator of a transit service.
 *
 * @property name Display name, the only field a provider always has and the only one shown.
 * @property id The operator's identifier in the provider's own namespace.
 * @property website Where to check for disruptions.
 */
@Serializable
data class TransitAgencyDto(
    @SerialName("name") val name: String,
    @SerialName("id") val id: String? = null,
    @SerialName("website") val website: String? = null,
)

/**
 * A stop on a journey: where the traveller boards, alights, or passes through.
 *
 * One type for all three, because they are the same thing seen at different moments and a screen
 * that draws a timeline draws them the same way.
 *
 * @property place Where it is.
 * @property name The name on the sign.
 * @property arrival When the vehicle gets there.
 * @property departure When it leaves, which differs from [arrival] by [dwellSeconds].
 * @property dwellSeconds How long the vehicle stands there.
 * @property offset Index into the ride's geometry where this stop falls, which is what puts a
 *   marker on the map without looking the stop up again.
 * @property url Where the operator publishes the state of this station.
 * @property wheelchairAccessible Whether the stop can be reached in a wheelchair.
 */
@Serializable
data class TransitStopDto(
    @SerialName("place") val place: GeoPoint,
    @SerialName("name") val name: String? = null,
    @SerialName("arrival") val arrival: ZonedTime? = null,
    @SerialName("departure") val departure: ZonedTime? = null,
    @SerialName("dwellSeconds") val dwellSeconds: Int? = null,
    @SerialName("offset") val offset: Int? = null,
    @SerialName("url") val url: String? = null,
    @SerialName("wheelchairAccessible") val wheelchairAccessible: WheelchairAccess = WheelchairAccess.UNKNOWN,
)

/**
 * How well a vehicle or a stop serves a traveller in a wheelchair.
 *
 * [UNKNOWN] is the default rather than an absent value: a feed that says nothing and a feed that has
 * not been asked are the same thing to the traveller, and neither may be shown as "no".
 */
@Serializable
enum class WheelchairAccess {
    /** The feed says nothing about it. */
    @SerialName("UNKNOWN")
    UNKNOWN,

    /** Reachable, or able to carry at least one wheelchair. */
    @SerialName("YES")
    YES,

    /** Possible, but with a restriction or with help from staff. */
    @SerialName("LIMITED")
    LIMITED,

    /** Not possible. */
    @SerialName("NO")
    NO,
}
