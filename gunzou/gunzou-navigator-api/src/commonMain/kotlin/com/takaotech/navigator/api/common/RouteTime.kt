package com.takaotech.navigator.api.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * When the traveller wants to be moving.
 *
 * An [Instant] rather than a local date and time: the provider needs an unambiguous point in time,
 * and the origin's own offset is something the server knows and the client does not necessarily.
 *
 * A provider that cannot honour [ArriveBy] must reject the request with
 * [com.takaotech.navigator.api.error.ErrorCode.UNSUPPORTED_OPTION] rather than silently treating it
 * as [DepartAt]; whether it can is published as
 * [com.takaotech.navigator.api.catalog.ProviderProfileDescriptor.supportsArriveBy].
 */
@Serializable
sealed interface RouteTime {
    /** Leave as soon as possible. The provider uses live traffic where it has it. */
    @Serializable
    @SerialName("now")
    data object Now : RouteTime

    /** Leave at [instant]. */
    @Serializable
    @SerialName("departAt")
    data class DepartAt(val instant: Instant) : RouteTime

    /** Arrive no later than [instant]; the departure time is computed backwards from it. */
    @Serializable
    @SerialName("arriveBy")
    data class ArriveBy(val instant: Instant) : RouteTime
}
