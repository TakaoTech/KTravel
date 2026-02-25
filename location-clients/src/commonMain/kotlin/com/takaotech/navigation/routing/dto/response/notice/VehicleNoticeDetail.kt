package com.takaotech.navigation.routing.dto.response.notice

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class VehicleNoticeDetail {

    /**
     * Detail indicating a general vehicle restriction.
     *
     * @property title The title of the restriction.
     * @property cause The cause of the restriction.
     * @property maxWeight The maximum weight restriction.
     * @property timeDependent Indicates if the restriction depends on time.
     * @property restrictedTimes The times when it is restricted.
     * @property unconditional Indicates if the restriction is unconditional.
     */
    @Serializable
    @SerialName("restriction")
    data class VehicleRestriction(
        @SerialName("title") val title: String? = null,
        @SerialName("cause") val cause: String? = null,
        @SerialName("maxWeight") val maxWeight: VehicleRestrictionMaxWeight? = null,
        @SerialName("timeDependent") val timeDependent: Boolean? = null,
        @SerialName("restrictedTimes") val restrictedTimes: String? = null,
        @SerialName("unconditional") val unconditional: Boolean? = null
    ) : VehicleNoticeDetail()

    /**
     * Detail indicating a violated transport mode.
     *
     * @property title The title of the violation.
     * @property cause The cause of the violation.
     */
    @Serializable
    @SerialName("violatedTransportMode")
    data class ViolatedTransportMode(
        @SerialName("title") val title: String? = null,
        @SerialName("cause") val cause: String? = null
    ) : VehicleNoticeDetail()

    /**
     * Detail indicating a violated truck road type.
     *
     * @property title The title of the violation.
     * @property cause The cause of the violation.
     * @property truckRoadType The type of truck road violated.
     */
    @Serializable
    @SerialName("truckRoadType")
    data class ViolatedTruckRoadType(
        @SerialName("title") val title: String? = null,
        @SerialName("cause") val cause: String? = null,
        @SerialName("truckRoadType") val truckRoadType: String? = null
    ) : VehicleNoticeDetail()

    /**
     * Detail indicating a violated zone reference.
     *
     * @property title The title of the violation.
     * @property cause The cause of the violation.
     * @property routingZoneRef The reference to the routing zone.
     * @property timeDependent Indicates if it depends on time.
     * @property restrictedTimes The restricted times.
     * @property licensePlateRestriction License plate restriction details.
     * @property maxWeight Max weight restriction details.
     */
    @Serializable
    @SerialName("zoneReference")
    data class ViolatedZoneReference(
        @SerialName("title") val title: String? = null,
        @SerialName("cause") val cause: String? = null,
        @SerialName("routingZoneRef") val routingZoneRef: String? = null,
        @SerialName("timeDependent") val timeDependent: Boolean? = null,
        @SerialName("restrictedTimes") val restrictedTimes: String? = null,
        @SerialName("licensePlateRestriction") val licensePlateRestriction: LicensePlateRestriction? = null,
        @SerialName("maxWeight") val maxWeight: VehicleRestrictionMaxWeight? = null
    ) : VehicleNoticeDetail()

    /**
     * Detail indicating a violated opening hours restriction for a charging station.
     *
     * @property title The title of the violation.
     * @property cause The cause of the violation.
     * @property openingHours The opening hours specification.
     */
    @Serializable
    @SerialName("violatedOpeningHours")
    data class ViolatedChargingStationOpeningHours(
        @SerialName("title") val title: String? = null,
        @SerialName("cause") val cause: String? = null,
        @SerialName("opening_hours") val openingHours: String? = null
    ) : VehicleNoticeDetail()
}
