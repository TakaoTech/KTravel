package com.takaotech.gunzou.api.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * How a single section of a route is travelled, at the granularity every provider agrees on.
 *
 * This is the coarse mode: a section served by a public transport vehicle is [TRANSIT] whatever the
 * vehicle is, and the vehicle itself is described by [TransitMode] inside
 * [com.takaotech.gunzou.api.response.TransitDetailsDto]. Keeping the two apart is what lets a
 * road route and a multimodal one share the same section type.
 */
@Serializable
enum class TravelMode {
    /** A private car. */
    @SerialName("CAR")
    CAR,

    /** A goods vehicle, which is routed under its own weight and dimension restrictions. */
    @SerialName("TRUCK")
    TRUCK,

    /** A car with the access rights of a taxi, such as reserved lanes. */
    @SerialName("TAXI")
    TAXI,

    /** A coach or bus driven by the traveller's own party, not a scheduled service. */
    @SerialName("BUS")
    BUS,

    /** On foot. */
    @SerialName("PEDESTRIAN")
    PEDESTRIAN,

    /** By bicycle. */
    @SerialName("BICYCLE")
    BICYCLE,

    /** A moped or motor scooter. */
    @SerialName("SCOOTER")
    SCOOTER,

    /** Aboard a scheduled public transport service, described by [TransitMode]. */
    @SerialName("TRANSIT")
    TRANSIT,

    /**
     * A mode this contract does not name yet.
     *
     * Exists so a provider that reports something new degrades to a drawable section instead of
     * failing to decode on a client built before it.
     */
    @SerialName("OTHER")
    OTHER,
}

/**
 * The kind of vehicle serving a [TravelMode.TRANSIT] section.
 *
 * The values follow the vocabulary shared by HERE Public Transit, GTFS and OpenTripPlanner, so a
 * second provider maps onto them without a translation table of its own.
 */
@Serializable
enum class TransitMode {
    /** Long distance high speed rail, such as a Frecciarossa or a TGV. */
    @SerialName("HIGH_SPEED_TRAIN")
    HIGH_SPEED_TRAIN,

    /** Long distance rail between major cities. */
    @SerialName("INTERCITY_TRAIN")
    INTERCITY_TRAIN,

    /** Rail between regions, stopping more often than an [INTERCITY_TRAIN]. */
    @SerialName("INTER_REGIONAL_TRAIN")
    INTER_REGIONAL_TRAIN,

    /** Rail within a region. */
    @SerialName("REGIONAL_TRAIN")
    REGIONAL_TRAIN,

    /** Commuter rail within a city and its suburbs, such as an S-Bahn or an RER. */
    @SerialName("CITY_TRAIN")
    CITY_TRAIN,

    /** A scheduled bus. */
    @SerialName("BUS")
    BUS,

    /** A bus run by a private operator outside the public network. */
    @SerialName("PRIVATE_BUS")
    PRIVATE_BUS,

    /** A bus on a dedicated right of way, running like a light rail line. */
    @SerialName("BUS_RAPID")
    BUS_RAPID,

    /** A passenger boat. */
    @SerialName("FERRY")
    FERRY,

    /** An underground metro. */
    @SerialName("SUBWAY")
    SUBWAY,

    /** A tram or street level light rail. */
    @SerialName("LIGHT_RAIL")
    LIGHT_RAIL,

    /** A monorail. */
    @SerialName("MONORAIL")
    MONORAIL,

    /** A funicular or rack railway climbing a slope. */
    @SerialName("INCLINED")
    INCLINED,

    /** A cable car or gondola. */
    @SerialName("AERIAL")
    AERIAL,

    /** A scheduled flight. */
    @SerialName("FLIGHT")
    FLIGHT,

    /**
     * A kind of vehicle this contract does not name yet.
     *
     * Present for the same forward compatibility reason as [TravelMode.OTHER]; it carries no meaning
     * in a request filter and the server ignores it there.
     */
    @SerialName("OTHER")
    OTHER,
}
