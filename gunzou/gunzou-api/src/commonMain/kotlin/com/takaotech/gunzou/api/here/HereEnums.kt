package com.takaotech.gunzou.api.here

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// The HERE vocabulary, copied rather than re-exported from :gunzou-here-client.
//
// Two reasons, and both matter. The vendor module must not appear in the dependencies of anything
// that talks to the server — it is the whole point of putting the server in front of it — and this
// contract has to be free to keep a value HERE removes, or to withhold one it adds, without the
// wire format changing under a client that was built yesterday. The cost is a translation from
// these types to the vendor DTOs, which lives in the server, in one file.

/**
 * How the traveller moves on the road profile.
 *
 * All of these reach the same upstream HERE endpoint with a different `transportMode`, but they do
 * not share one path here: each is asked for on
 * [its own][com.takaotech.gunzou.api.NavigatorApi.hereRouting], such as
 * `/v1/here/routing/pedestrian`. The mode is therefore never a field of the request body.
 */
@Serializable
enum class HereTransportMode {
    /** A private car. */
    @SerialName("CAR")
    CAR,

    /** A goods vehicle, routed under its own weight and dimension restrictions. */
    @SerialName("TRUCK")
    TRUCK,

    /** On foot. */
    @SerialName("PEDESTRIAN")
    PEDESTRIAN,

    /** By bicycle. */
    @SerialName("BICYCLE")
    BICYCLE,

    /** A moped or motor scooter. */
    @SerialName("SCOOTER")
    SCOOTER,

    /** A car with the access rights of a taxi, such as reserved lanes. */
    @SerialName("TAXI")
    TAXI,

    /** A coach driven by the traveller's own party, not a scheduled service. */
    @SerialName("BUS")
    BUS,

    /** A coach run by a private operator, which HERE routes under its own access rules. */
    @SerialName("PRIVATE_BUS")
    PRIVATE_BUS,
    ;

    /**
     * How this mode is spelled in a path.
     *
     * Not `name.lowercase()`: that turns `PRIVATE_BUS` into `private_bus`, which is neither what
     * HERE calls it nor what [fromPathSegment] would read back.
     */
    val pathSegment: String
        get() = when (this) {
            PRIVATE_BUS -> "privateBus"
            else -> name.lowercase()
        }

    /**
     * Whether travelling this way can be charged a toll.
     *
     * A walk and a bicycle ride cannot, so asking for
     * [HereReturnAttribute.TOLLS] on one is a question with no answer rather than an answer of zero
     * — and it is paid for upstream all the same. Declared here, on the vocabulary itself, because
     * both the client that builds the request and the server that refuses it need the same fact.
     */
    val hasTolls: Boolean
        get() = this != PEDESTRIAN && this != BICYCLE

    /** How a mode is read back out of a path. */
    companion object {
        /** The mode this path segment names, or `null` when it names none. */
        fun fromPathSegment(segment: String): HereTransportMode? =
            entries.firstOrNull { it.pathSegment.equals(segment, ignoreCase = true) }
    }
}

/** What the router optimizes for. */
@Serializable
enum class HereRoutingMode {
    /** Shortest travel time. */
    @SerialName("FAST")
    FAST,

    /** Shortest distance, regardless of how long it takes. */
    @SerialName("SHORT")
    SHORT,
}

/**
 * Extra data to compute and return.
 *
 * Every entry costs something upstream, so the request asks for what it will draw and nothing more.
 * The dependencies HERE imposes between them — actions need the polyline, instructions need the
 * actions — are enforced by the server before the call, not here: a request that violates one must
 * come back as [com.takaotech.gunzou.api.error.ErrorCode.INVALID_REQUEST] rather than as a
 * failure to construct the object.
 */
@Serializable
enum class HereReturnAttribute {
    /** The geometry of each section. */
    @SerialName("POLYLINE")
    POLYLINE,

    /** The manoeuvres. Requires [POLYLINE]. */
    @SerialName("ACTIONS")
    ACTIONS,

    /** Localized text for each manoeuvre. Requires [ACTIONS]. */
    @SerialName("INSTRUCTIONS")
    INSTRUCTIONS,

    /** Duration and distance per section. */
    @SerialName("SUMMARY")
    SUMMARY,

    /** Duration and distance of the travelling part of a section, excluding waits. */
    @SerialName("TRAVEL_SUMMARY")
    TRAVEL_SUMMARY,

    /** Everything needed to drive turn by turn guidance. Requires [POLYLINE]. */
    @SerialName("TURN_BY_TURN_ACTIONS")
    TURN_BY_TURN_ACTIONS,

    /** Duration under typical rather than current traffic. */
    @SerialName("TYPICAL_DURATION")
    TYPICAL_DURATION,

    /** Elevation as a third dimension in the geometry. */
    @SerialName("ELEVATION")
    ELEVATION,

    /** What has to be paid along the way. */
    @SerialName("TOLLS")
    TOLLS,

    /** The incidents affecting each section. */
    @SerialName("INCIDENTS")
    INCIDENTS,

    /** The names and road numbers that tell this alternative apart from the others. */
    @SerialName("ROUTE_LABELS")
    ROUTE_LABELS,
    ;

    /** The combinations worth naming, so a caller does not assemble them by hand every time. */
    companion object {
        /** Enough to draw the route and label it. */
        val BASIC: List<HereReturnAttribute> = listOf(POLYLINE, SUMMARY)

        /** Enough to guide someone along it, which is what the planning screen asks for. */
        val NAVIGATION: List<HereReturnAttribute> = listOf(POLYLINE, ACTIONS, INSTRUCTIONS, SUMMARY)

        /** [NAVIGATION] plus what the trip will cost in tolls. */
        val NAVIGATION_WITH_TOLLS: List<HereReturnAttribute> =
            listOf(POLYLINE, ACTIONS, INSTRUCTIONS, SUMMARY, TOLLS)
    }
}

/**
 * Things the route should stay away from.
 *
 * Absent and empty mean the same thing, so a caller that has nothing to avoid can leave the whole
 * object out.
 *
 * @property features Road and crossing types to keep off the route where possible. A provider may
 *   still route through one when there is no alternative, and says so with a notice.
 */
@Serializable
data class HereAvoidOptions(@SerialName("features") val features: List<HereAvoidFeature> = emptyList())

/** A kind of road or crossing a route can be asked to avoid. */
@Serializable
enum class HereAvoidFeature {
    /** Any road that charges a toll. */
    @SerialName("TOLL_ROAD")
    TOLL_ROAD,

    /** Motorways and other roads reachable only through a junction. */
    @SerialName("CONTROLLED_ACCESS_HIGHWAY")
    CONTROLLED_ACCESS_HIGHWAY,

    /** Crossings by boat. */
    @SerialName("FERRY")
    FERRY,

    /** Trains that carry the vehicle through a tunnel or over a pass. */
    @SerialName("CAR_SHUTTLE_TRAIN")
    CAR_SHUTTLE_TRAIN,

    /** Tunnels, which some vehicles and cargoes may not enter. */
    @SerialName("TUNNEL")
    TUNNEL,

    /** Unpaved roads. */
    @SerialName("DIRT_ROAD")
    DIRT_ROAD,

    /** Turns that are hard to take, such as an unprotected left across traffic. */
    @SerialName("DIFFICULT_TURNS")
    DIFFICULT_TURNS,

    /** Manoeuvres that reverse the direction of travel. */
    @SerialName("U_TURNS")
    U_TURNS,

    /** Roads that close for part of the year, such as an Alpine pass in winter. */
    @SerialName("SEASONAL_CLOSURE")
    SEASONAL_CLOSURE,
}
