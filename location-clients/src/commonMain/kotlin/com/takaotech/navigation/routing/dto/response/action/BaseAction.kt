package com.takaotech.navigation.routing.dto.response.action

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * An action to be performed between sections or during a section.
 *
 * @property action The type of the action. NOTE: The list of possible actions may be extended in the future. The client application should handle such a case gracefully.
 * @property duration Estimated duration of this action (in seconds). Actions last until the next action, or the end of the route in case of the last one.
 * @property length Estimated length of this action (in meters). Actions last until the next action, or the end of the route in case of the last one.
 * @property instruction Description of the action (e.g. Turn left onto Minna St.).
 * @property offset Actions offsets represent the coordinate index in the polyline.
 */
@Serializable
data class BaseAction(
    @SerialName("action") val action: String? = null,
    @SerialName("duration") val duration: Int? = null,
    @SerialName("length") val length: Int? = null,
    @SerialName("instruction") val instruction: String? = null,
    @SerialName("offset") val offset: Int? = null
)
