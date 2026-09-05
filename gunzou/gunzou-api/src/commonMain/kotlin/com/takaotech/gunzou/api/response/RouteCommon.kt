package com.takaotech.gunzou.api.response

import com.takaotech.gunzou.api.common.GeoPoint
import com.takaotech.gunzou.api.common.ZonedTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// What a road answer and a journey answer genuinely have in common.
//
// The list is short on purpose. These four types are here because a duration is a duration and a
// manoeuvre is a manoeuvre whichever engine produced it — not because two responses happened to be
// one type once. Everything that differs lives in RoutingRouteResponse or TransitJourneyResponse, and
// a field that only makes sense to one of them belongs there rather than here with a comment
// explaining when it is null.

/**
 * Totals for a route, a journey, or one of their legs.
 *
 * Always metric, whatever [com.takaotech.gunzou.api.common.Units] the request asked for.
 *
 * @property durationSeconds Expected travel time under the traffic assumed for the request.
 * @property distanceMeters Length travelled.
 * @property baseDurationSeconds Travel time with no traffic at all, when the provider reports it.
 *   The difference against [durationSeconds] is the delay the user is being warned about.
 */
@Serializable
data class RouteSummaryDto(
    @SerialName("durationSeconds") val durationSeconds: Long,
    @SerialName("distanceMeters") val distanceMeters: Int,
    @SerialName("baseDurationSeconds") val baseDurationSeconds: Long? = null,
)

/**
 * A single manoeuvre inside a leg travelled under the traveller's own direction.
 *
 * @property action What to do, in the provider's own vocabulary — `turn`, `depart`, `arrive`,
 *   `roundaboutExit`. Left as a string because normalizing it would have to be redone for every
 *   engine and the user interface only ever shows [instruction].
 * @property durationSeconds How long this manoeuvre takes.
 * @property distanceMeters How far it runs, absent when the manoeuvre has no length of its own.
 * @property instruction The sentence to show or speak, localized by the provider.
 * @property offset Index into the leg's geometry where this manoeuvre begins.
 * @property direction Which way to turn, when the action implies one.
 * @property severity How sharp the turn is.
 */
@Serializable
data class RouteActionDto(
    @SerialName("action") val action: String,
    @SerialName("durationSeconds") val durationSeconds: Long,
    @SerialName("distanceMeters") val distanceMeters: Int? = null,
    @SerialName("instruction") val instruction: String? = null,
    @SerialName("offset") val offset: Int? = null,
    @SerialName("direction") val direction: String? = null,
    @SerialName("severity") val severity: String? = null,
)

/**
 * An end of a leg that is a plain place rather than a stop on a timetable.
 *
 * @property place Where it is.
 * @property time When the traveller is there, absent when the provider did not schedule the leg.
 * @property name The name of the place, when the provider has one.
 */
@Serializable
data class RouteWaypointDto(
    @SerialName("place") val place: GeoPoint,
    @SerialName("time") val time: ZonedTime? = null,
    @SerialName("name") val name: String? = null,
)

/**
 * An advisory attached to an answer, to one of its routes, or to one of its journeys.
 *
 * @property code The provider's own code, kept verbatim so it stays greppable against their docs.
 * @property title A human readable rendering of [code], when the provider supplies one.
 * @property severity Whether the answer is still usable.
 */
@Serializable
data class NoticeDto(
    @SerialName("code") val code: String,
    @SerialName("title") val title: String? = null,
    @SerialName("severity") val severity: NoticeSeverity = NoticeSeverity.INFO,
)

/** How much a [NoticeDto] should worry the caller. */
@Serializable
enum class NoticeSeverity {
    /** Worth showing, but the answer is valid. */
    @SerialName("INFO")
    INFO,

    /** The answer is returned but is known to be violating something the request asked for. */
    @SerialName("CRITICAL")
    CRITICAL,
}
