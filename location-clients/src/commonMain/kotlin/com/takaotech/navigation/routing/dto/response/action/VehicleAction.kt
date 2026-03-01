package com.takaotech.navigation.routing.dto.response.action

import com.takaotech.navigation.routing.dto.response.road.ExitInfo
import com.takaotech.navigation.routing.dto.response.road.LocalizedString
import com.takaotech.navigation.routing.dto.response.road.RoadInfo
import com.takaotech.navigation.routing.dto.response.road.SignpostInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Action attached to a vehicle section.
 *
 * @property action The type of the action. NOTE: The list of possible actions may be extended in the future.
 * @property duration Estimated duration of this action (in seconds). Actions last until the next action, or the end of the route in case of the last one.
 * @property length Estimated length of this action (in meters). Actions last until the next action, or the end of the route in case of the last one.
 * @property instruction Description of the action (e.g. Turn left onto Minna St.).
 * @property offset Actions offsets represent the coordinate index in the polyline.
 * @property direction The direction of the action.
 * @property severity The severity of the action.
 * @property currentRoad Information about the road the action starts from.
 * @property nextRoad Information about the road the action leads to.
 * @property exitSign Details of the exit sign.
 * @property signPost Details of the signpost.
 * @property intersectionName Name of the intersection.
 * @property turnAngle Turn angle in degrees.
 */
@Serializable
data class VehicleAction(
    @SerialName("action") val action: String,
    @SerialName("duration") val duration: Int,
    @SerialName("length") val length: Int,
    @SerialName("instruction") val instruction: String? = null,
    @SerialName("offset") val offset: Int? = null,
    @SerialName("direction") val direction: String? = null,
    @SerialName("severity") val severity: String? = null,
    @SerialName("currentRoad") val currentRoad: RoadInfo? = null,
    @SerialName("nextRoad") val nextRoad: RoadInfo? = null,
    @SerialName("exitSign") val exitSign: ExitInfo? = null,
    @SerialName("signpost") val signPost: SignpostInfo? = null,
    @SerialName("intersectionName") val intersectionName: List<LocalizedString>? = null,
    @SerialName("turnAngle") val turnAngle: Double? = null
)
