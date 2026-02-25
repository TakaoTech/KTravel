package com.takaotech.navigation.routing.dto.response

import com.takaotech.navigation.routing.dto.response.action.BaseAction
import com.takaotech.navigation.routing.dto.response.action.Passthrough
import com.takaotech.navigation.routing.dto.response.action.VehicleAction
import com.takaotech.navigation.routing.dto.response.action.VehiclePostAction
import com.takaotech.navigation.routing.dto.response.common.RefReplacements
import com.takaotech.navigation.routing.dto.response.incident.TrafficIncident
import com.takaotech.navigation.routing.dto.response.notice.VehicleNoticeDetail
import com.takaotech.navigation.routing.dto.response.span.VehicleSpan
import com.takaotech.navigation.routing.dto.response.summary.ConsumptionType
import com.takaotech.navigation.routing.dto.response.summary.VehicleTravelSummary
import com.takaotech.navigation.routing.dto.response.toll.TollCost
import com.takaotech.navigation.routing.dto.response.toll.TollSystem
import com.takaotech.navigation.routing.dto.response.zone.RoutingZone
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represent a section of a route
 *
 * @property id Unique identifier of the section
 * @property type Section type used by the client to identify what extension to the BaseSection are available. (always "vehicle" for this type)
 * @property preActions Actions that must be done prior to `departure`.
 * @property actions Actions to be performed at or during a specific portion of a section. Action offsets represent the coordinate index in the polyline.
 * @property language Language of the localized strings in the section, if any, in BCP47 format.
 * @property postActions Actions that must be done after `arrival`.
 * @property turnByTurnActions Turn-by-turn guidance actions. Action offsets represent the coordinate index in the polyline.
 * @property departure Departure information
 * @property arrival Arrival information
 * @property passthrough List of via waypoints this section is passing through.
 * @property summary The total value of key attributes, such as duration, length, and consumption, summed up for the entire section, including `preActions`, `postActions`, and the travel portion of the section.
 * @property travelSummary The total value of key attributes, such as duration, length, and consumption, summed up only for the travel portion of the section, between `departure` and `arrival`. `preActions` and `postActions` are excluded.
 * @property polyline Encoded polyline of the route geometry
 * @property notices Contains a list of issues related to this section of the route. Notices must be carefully evaluated and, if deemed necessary, the route section should be discarded accordingly.
 * @property spans Spans attached to a `Section` describing vehicle content.
 * @property routingZones A list of routing zones that are applicable to the section.
 * @property truckRoadTypes A list of truck road types that are applicable to the section.
 * @property incidents A list of all incidents that apply to the section.
 * @property refReplacements Reference replacements
 * @property tollSystems An array of toll authorities that collect payments for the use of (part of) the specified section of the route.
 * @property tolls Detail of tolls to be paid for traversing the specified section.
 * @property consumptionType Vehicle energy or fuel consumption type.
 * @property noThroughRestrictions A list of all rules that restrict movement through the area without there being an origin, destination, or `via` waypoint inside of it. Only restrictions applicable to the vehicle for which the route was requested are returned.
 * @property transport Transport mode information
 */
@Serializable
data class RouterSection(
    @SerialName("id") val id: String,
    @SerialName("type") val type: String,
    @SerialName("preActions") val preActions: List<BaseAction>? = null,
    @SerialName("actions") val actions: List<VehicleAction>? = null,
    @SerialName("language") val language: String? = null,
    @SerialName("postActions") val postActions: List<VehiclePostAction>? = null,
    @SerialName("turnByTurnActions") val turnByTurnActions: List<VehicleAction>? = null,
    @SerialName("departure") val departure: VehicleDeparture,
    @SerialName("arrival") val arrival: VehicleDeparture,
    @SerialName("passthrough") val passthrough: List<Passthrough>? = null,
    @SerialName("summary") val summary: VehicleSummary? = null,
    @SerialName("travelSummary") val travelSummary: VehicleTravelSummary? = null,
    @SerialName("polyline") val polyline: String? = null,
    @SerialName("notices") val notices: List<Notice>? = null,
    @SerialName("spans") val spans: List<VehicleSpan>? = null,
    @SerialName("routingZones") val routingZones: List<RoutingZone>? = null,
    @SerialName("truckRoadTypes") val truckRoadTypes: List<String>? = null,
    @SerialName("incidents") val incidents: List<TrafficIncident>? = null,
    @SerialName("refReplacements") val refReplacements: RefReplacements? = null,
    @SerialName("tollSystems") val tollSystems: List<TollSystem>? = null,
    @SerialName("tolls") val tolls: List<TollCost>? = null,
    @SerialName("consumptionType") val consumptionType: ConsumptionType? = null,
    @SerialName("noThroughRestrictions") val noThroughRestrictions: List<VehicleNoticeDetail.VehicleRestriction>? = null,
    @SerialName("transport") val transport: VehicleTransport
)
