@file:OptIn(ExperimentalTime::class)

package com.takaotech.ktravel.domain.routing.model

import kotlinx.datetime.LocalDateTime
import kotlin.time.ExperimentalTime

/**
 * What a calculation answered, as the plan keeps it.
 *
 * Sealed for the same reason [RouteResult] is: a road route and a journey on scheduled services are
 * not one thing described twice. They used to be flattened into a single `Route` on the way into the
 * plan, and that shape was poorer than either — a saved journey came back without the line it was
 * taken on, the stops it called at, or who ran it, because there was nowhere to put them. Keeping
 * the two apart means what is saved is what was answered.
 *
 * The variants hold the answer types themselves rather than reduced copies of them, so there is one
 * model per profile and not one more.
 */
sealed interface TransportAnswer {

    /**
     * Totals for the whole leg: what the day timeline and the metrics panel show.
     *
     * Note the two are not measured the same way, and deliberately so — see [Transit.summary].
     */
    val summary: RouteSummary

    /** The drawable shapes, in travel order, each with the colour of the line when it has one. */
    val paths: List<TransportPath>

    /**
     * The vehicle the leg is filed under, in the navigator's vocabulary.
     *
     * Null when the answer carries no mode at all, which no real answer does.
     */
    val principalMode: String?

    /**
     * When the traveller sets off, as the clock reads where the leg starts.
     *
     * Null when the calculation produced no times at all, which is what a route asked for "now"
     * comes back as: an hour invented here would be worse than none.
     */
    val departureTime: LocalDateTime?

    /** When they arrive, with the same rule as [departureTime]. */
    val arrivalTime: LocalDateTime?

    /**
     * A route on roads, travelled by one vehicle.
     *
     * @property route The alternative the traveller confirmed, exactly as the navigator answered it.
     */
    data class Routing(val route: RoutingRoute) : TransportAnswer {

        override val summary: RouteSummary get() = route.summary

        override val paths: List<TransportPath>
            get() = route.sections.mapNotNull { section -> section.polyline?.let { TransportPath(it) } }

        override val principalMode: String? get() = route.sections.firstOrNull()?.mode

        /** The first section the navigator dated, which is where the road leg starts being timed. */
        override val departureTime: LocalDateTime?
            get() = route.sections.firstNotNullOfOrNull { it.departure?.localDateTime() }

        override val arrivalTime: LocalDateTime?
            get() = route.sections.asReversed().firstNotNullOfOrNull { it.arrival?.localDateTime() }
    }

    /**
     * A journey on scheduled services: some walking, and one or more vehicles.
     *
     * @property journey The alternative the traveller confirmed, lines and stops included.
     */
    data class Transit(val journey: TransitJourney) : TransportAnswer {

        /**
         * Door to door, waits included, when the feed timed both ends.
         *
         * [TransitJourney.summary] is time *travelled*, which is not how long the traveller is out:
         * a wait on a platform belongs to neither step and would silently vanish from the day.
         */
        override val summary: RouteSummary
            get() = RouteSummary(
                durationSeconds = journey.totalDuration ?: journey.summary.durationSeconds,
                distance = journey.summary.distance,
            )

        override val paths: List<TransportPath>
            get() = journey.steps.mapNotNull { step ->
                step.polyline?.let { TransportPath(it, (step as? TransitStep.Ride)?.line?.color) }
            }

        /**
         * The first vehicle and not the first step: a journey almost always starts on foot, and
         * taking the mode of that walk filed every train ride in the plan as a walk.
         */
        override val principalMode: String? get() = journey.rides.firstOrNull()?.line?.mode

        /** A journey knows its own times: they are the timetable it was read off. */
        override val departureTime: LocalDateTime? get() = journey.departure?.atStop()

        override val arrivalTime: LocalDateTime? get() = journey.arrival?.atStop()
    }
}

/**
 * One shape to draw.
 *
 * @property polyline The geometry, in HERE flexible encoding.
 * @property colorHex The line colour as `#RRGGBB`, and not a `Color`: this is the domain, so the map
 * turns it into one where the map lives. Null on foot and on the road, which have no line.
 */
data class TransportPath(val polyline: String, val colorHex: String? = null)
