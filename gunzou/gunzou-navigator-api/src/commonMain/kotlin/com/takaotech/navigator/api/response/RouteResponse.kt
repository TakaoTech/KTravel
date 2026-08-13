package com.takaotech.navigator.api.response

import com.takaotech.navigator.api.common.GeoPoint
import com.takaotech.navigator.api.common.ProviderId
import com.takaotech.navigator.api.common.ProviderProfile
import com.takaotech.navigator.api.common.TravelMode
import com.takaotech.navigator.api.common.ZonedTime
import kotlinx.serialization.Serializable

/**
 * The answer of every routing endpoint, whichever provider computed it.
 *
 * This is the point of the whole server: the request models differ per provider because the inputs
 * genuinely differ, while the result the user interface draws is one shape. [provider] and [profile]
 * are echoed back so a client that fans out to several endpoints can tell the answers apart without
 * tracking which call each one came from.
 *
 * @property provider The engine that computed these routes.
 * @property profile The profile of that engine, that is which of its APIs was called.
 * @property routes The alternatives, best first. Empty is not an answer: a provider that finds
 *   nothing produces [com.takaotech.navigator.api.error.ErrorCode.NO_ROUTE_FOUND] instead.
 * @property notices Advisories about the whole answer, such as an option that was accepted but
 *   could not be honoured. Never a substitute for an error.
 */
@Serializable
data class RouteResponse(
    val provider: ProviderId,
    val profile: ProviderProfile,
    val routes: List<RouteDto> = emptyList(),
    val notices: List<NoticeDto> = emptyList(),
)

/**
 * One alternative from origin to destination.
 *
 * @property summary Totals for the whole alternative, aggregated by the server over [sections]. The
 *   provider is not asked for it: not all of them return one, and a client that had to sum the
 *   sections itself would be reimplementing the same loop on every platform.
 * @property sections The legs, in travel order. A road route usually has one per via waypoint; a
 *   transit route alternates walking and vehicle legs.
 */
@Serializable
data class RouteDto(val summary: RouteSummaryDto, val sections: List<RouteSectionDto> = emptyList())

/**
 * Totals for a route or one of its sections.
 *
 * Always metric, whatever [com.takaotech.navigator.api.common.Units] the request asked for.
 *
 * @property durationSeconds Expected travel time under the traffic assumed for the request.
 * @property distanceMeters Length travelled.
 * @property baseDurationSeconds Travel time with no traffic at all, when the provider reports it.
 *   The difference against [durationSeconds] is the delay the user is being warned about.
 */
@Serializable
data class RouteSummaryDto(val durationSeconds: Long, val distanceMeters: Int, val baseDurationSeconds: Long? = null)

/**
 * One leg of a route, travelled in a single [mode].
 *
 * [transit] is what lets a road profile and a public transport one share this type: it is null on
 * every section travelled under the traveller's own power or in their own vehicle, and populated on
 * the ones served by a scheduled vehicle. The same shape carries a multimodal provider, where the
 * two kinds alternate inside one route.
 *
 * @property summary Totals for this leg.
 * @property mode How this leg is travelled.
 * @property actions The manoeuvres to perform, when the request asked for them.
 * @property departure Where and when this leg starts.
 * @property arrival Where and when this leg ends.
 * @property geometry The drawable shape of this leg.
 * @property transit The scheduled service operating this leg, or null if there is none.
 * @property tollSystems The authorities collecting on this leg, referenced by index from [tolls].
 * @property tolls What has to be paid on this leg.
 */
@Serializable
data class RouteSectionDto(
    val summary: RouteSummaryDto,
    val mode: TravelMode,
    val actions: List<RouteActionDto> = emptyList(),
    val departure: RouteWaypointDto? = null,
    val arrival: RouteWaypointDto? = null,
    val geometry: RouteGeometry? = null,
    val transit: TransitDetailsDto? = null,
    val tollSystems: List<TollSystemDto> = emptyList(),
    val tolls: List<TollCostDto> = emptyList(),
)

/**
 * A single manoeuvre inside a section.
 *
 * @property action What to do, in the provider's own vocabulary — `turn`, `depart`, `arrive`,
 *   `roundaboutExit`. Left as a string because normalizing it would have to be redone for every
 *   engine and the user interface only ever shows [instruction].
 * @property durationSeconds How long this manoeuvre takes.
 * @property distanceMeters How far it runs, absent when the manoeuvre has no length of its own.
 * @property instruction The sentence to show or speak, localized by the provider.
 * @property offset Index into the section's geometry where this manoeuvre begins.
 * @property direction Which way to turn, when the action implies one.
 * @property severity How sharp the turn is.
 */
@Serializable
data class RouteActionDto(
    val action: String,
    val durationSeconds: Long,
    val distanceMeters: Int? = null,
    val instruction: String? = null,
    val offset: Int? = null,
    val direction: String? = null,
    val severity: String? = null,
)

/**
 * An end of a section.
 *
 * @property place Where it is.
 * @property time When the traveller is there, absent when the provider did not schedule the leg.
 * @property name The name of the place, such as a stop or a station.
 */
@Serializable
data class RouteWaypointDto(val place: GeoPoint, val time: ZonedTime? = null, val name: String? = null)

/**
 * An advisory attached to a response or to one of its routes.
 *
 * @property code The provider's own code, kept verbatim so it stays greppable against their docs.
 * @property title A human readable rendering of [code], when the provider supplies one.
 * @property severity Whether the answer is still usable.
 */
@Serializable
data class NoticeDto(val code: String, val title: String? = null, val severity: NoticeSeverity = NoticeSeverity.INFO)

/** How much a [NoticeDto] should worry the caller. */
@Serializable
enum class NoticeSeverity {
    /** Worth showing, but the route is valid. */
    INFO,

    /** The route is returned but is known to be violating something the request asked for. */
    CRITICAL,
}
