package com.takaotech.ktravel.domain.routing.model

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.asTimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration
import kotlin.time.Instant

/**
 * What a transit profile answered: ways of getting there on scheduled services.
 *
 * Separate from [RoutingRoutes] because the two describe different things. A road route is a shape with
 * manoeuvres along it; a journey is a sequence of departures the traveller has to be at on time, run
 * by operators, calling at stops. The screens that draw them have almost nothing in common, and the
 * one type they used to share made every road section carry an empty list of stops.
 *
 * @property journeys The alternatives, best first.
 */
data class TransitJourneys(val journeys: List<TransitJourney>)

/**
 * One way of getting there on scheduled services.
 *
 * @property summary Time and distance actually travelled, aggregated by the navigator. It is *not*
 *   how long the traveller will be out: a wait on a platform belongs to neither leg. Use
 *   [totalDuration] for that.
 * @property steps Walking and riding, alternating, in travel order.
 */
data class TransitJourney(val summary: RouteSummary, val steps: List<TransitStep>) {

    /** The vehicles taken, in order. */
    val rides: List<TransitStep.Ride> get() = steps.filterIsInstance<TransitStep.Ride>()

    /** How many times the traveller has to get off one vehicle and onto another. */
    val changes: Int get() = (rides.size - 1).coerceAtLeast(0)

    /** When the traveller sets off, which is the start of the first leg that has a time. */
    val departure: TransitTime? get() = steps.firstNotNullOfOrNull { it.departure }

    /** When they arrive, which is the end of the last leg that has one. */
    val arrival: TransitTime? get() = steps.lastOrNull { it.arrival != null }?.arrival

    /**
     * Door to door, waits included.
     *
     * Null when the feed timed neither end. Falls back to [RouteSummary.durationSeconds] nowhere on
     * purpose: a number that silently means something else is worse than no number.
     */
    val totalDuration: Duration?
        get() {
            val from = departure ?: return null
            val to = arrival ?: return null
            return to.instant - from.instant
        }
}

/**
 * One leg of a journey: either the traveller walks it, or a scheduled vehicle carries them.
 *
 * Sealed, so a screen's `when` is exhaustive and a third kind of leg cannot be added without every
 * screen being told what to draw for it.
 */
sealed interface TransitStep {

    /** Totals for this leg. */
    val summary: RouteSummary

    /** The drawable shape, in HERE flexible encoding. */
    val polyline: String?

    /** When the traveller sets off on this leg. */
    val departure: TransitTime?

    /** When they get to the end of it. */
    val arrival: TransitTime?

    /**
     * A stretch covered on foot: to the first stop, between two of them, or off the last one.
     *
     * @property from Where the walk starts, when the feed says.
     * @property to Where it ends.
     */
    data class Walk(
        override val summary: RouteSummary,
        override val polyline: String? = null,
        override val departure: TransitTime? = null,
        override val arrival: TransitTime? = null,
        val from: RouteLocation? = null,
        val to: RouteLocation? = null,
    ) : TransitStep

    /**
     * A stretch aboard a scheduled service.
     *
     * @property line The service as it is written on the vehicle.
     * @property agency Who runs it, which is who to ask about a disruption.
     * @property boarding The stop the traveller gets on at.
     * @property alighting The stop they get off at.
     * @property intermediateStops What the vehicle calls at in between. Empty means they were not
     *   asked for, not that the vehicle runs non stop.
     */
    data class Ride(
        override val summary: RouteSummary,
        val line: TransitLine,
        override val polyline: String? = null,
        val agency: TransitAgency? = null,
        val boarding: TransitStop? = null,
        val alighting: TransitStop? = null,
        val intermediateStops: List<TransitStop> = emptyList(),
    ) : TransitStep {
        override val departure: TransitTime? get() = boarding?.departure ?: boarding?.arrival
        override val arrival: TransitTime? get() = alighting?.arrival ?: alighting?.departure
    }
}

/**
 * The service operating a [TransitStep.Ride].
 *
 * @property mode The kind of vehicle, in the navigator's vocabulary.
 * @property name The line as it is written on the vehicle, such as `M1` or `62`.
 * @property category The operator's own wording for [mode], which is usually what the signage says.
 * @property headsign The destination on the front of the vehicle, which is how a traveller tells
 *   apart the two directions of the same line.
 * @property color Line colour as `#RRGGBB`, when the agency publishes one.
 */
data class TransitLine(
    val mode: String,
    val name: String? = null,
    val shortName: String? = null,
    val longName: String? = null,
    val category: String? = null,
    val headsign: String? = null,
    val color: String? = null,
    val textColor: String? = null,
    val url: String? = null,
    val wheelchairAccessible: WheelchairAccess = WheelchairAccess.UNKNOWN,
)

/** The operator of a service. */
data class TransitAgency(val name: String, val id: String? = null, val website: String? = null)

/**
 * A stop on a journey: where the traveller boards, alights, or passes through.
 *
 * @property offset Index into the ride's polyline, which is what puts a marker on the map without
 *   looking the stop up again.
 */
data class TransitStop(
    val location: RouteLocation,
    val name: String? = null,
    val arrival: TransitTime? = null,
    val departure: TransitTime? = null,
    val dwell: Duration? = null,
    val offset: Int? = null,
    val url: String? = null,
    val wheelchairAccessible: WheelchairAccess = WheelchairAccess.UNKNOWN,
)

/**
 * A moment on a timetable, together with the offset in force where it happens.
 *
 * Both halves are needed. The [instant] is what durations are computed on; the [offset] is what
 * turns it back into the time on the departure board, which is local to the stop and not to the
 * device the traveller is holding.
 */
data class TransitTime(val instant: Instant, val offset: UtcOffset) {

    /** The wall clock a traveller reads at that stop. */
    fun atStop(): LocalDateTime = instant.toLocalDateTime(offset.asTimeZone())

    /** The same moment where the device is, for a screen that would rather show one timezone. */
    fun onDevice(): LocalDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
}

/**
 * How well a vehicle or a stop serves a traveller in a wheelchair.
 *
 * [UNKNOWN] is the default rather than an absent value: a feed that says nothing and a feed that was
 * not asked are the same thing to the traveller, and neither may be shown as [NO].
 */
enum class WheelchairAccess {
    /** The feed says nothing about it. */
    UNKNOWN,

    /** Reachable, or able to carry at least one wheelchair. */
    YES,

    /** Possible, but with a restriction or with help from staff. */
    LIMITED,

    /** Not possible. */
    NO,
}
