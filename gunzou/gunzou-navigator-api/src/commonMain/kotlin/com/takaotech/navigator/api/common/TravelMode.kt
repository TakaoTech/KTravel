package com.takaotech.navigator.api.common

import kotlinx.serialization.Serializable

/**
 * How a single section of a route is travelled, at the granularity every provider agrees on.
 *
 * This is the coarse mode: a section served by a public transport vehicle is [TRANSIT] whatever the
 * vehicle is, and the vehicle itself is described by [TransitMode] inside
 * [com.takaotech.navigator.api.response.TransitDetailsDto]. Keeping the two apart is what lets a
 * road route and a multimodal one share the same section type.
 */
@Serializable
enum class TravelMode {
    /** A private car. */
    CAR,

    /** A goods vehicle, which is routed under its own weight and dimension restrictions. */
    TRUCK,

    /** A car with the access rights of a taxi, such as reserved lanes. */
    TAXI,

    /** A coach or bus driven by the traveller's own party, not a scheduled service. */
    BUS,

    /** On foot. */
    PEDESTRIAN,

    /** By bicycle. */
    BICYCLE,

    /** A moped or motor scooter. */
    SCOOTER,

    /** Aboard a scheduled public transport service, described by [TransitMode]. */
    TRANSIT,

    /**
     * A mode this contract does not name yet.
     *
     * Exists so a provider that reports something new degrades to a drawable section instead of
     * failing to decode on a client built before it.
     */
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
    HIGH_SPEED_TRAIN,

    /** Long distance rail between major cities. */
    INTERCITY_TRAIN,

    /** Rail between regions, stopping more often than an [INTERCITY_TRAIN]. */
    INTER_REGIONAL_TRAIN,

    /** Rail within a region. */
    REGIONAL_TRAIN,

    /** Commuter rail within a city and its suburbs, such as an S-Bahn or an RER. */
    CITY_TRAIN,

    /** A scheduled bus. */
    BUS,

    /** A bus run by a private operator outside the public network. */
    PRIVATE_BUS,

    /** A bus on a dedicated right of way, running like a light rail line. */
    BUS_RAPID,

    /** A passenger boat. */
    FERRY,

    /** An underground metro. */
    SUBWAY,

    /** A tram or street level light rail. */
    LIGHT_RAIL,

    /** A monorail. */
    MONORAIL,

    /** A funicular or rack railway climbing a slope. */
    INCLINED,

    /** A cable car or gondola. */
    AERIAL,

    /** A scheduled flight. */
    FLIGHT,

    /**
     * A kind of vehicle this contract does not name yet.
     *
     * Present for the same forward compatibility reason as [TravelMode.OTHER]; it carries no meaning
     * in a request filter and the server ignores it there.
     */
    OTHER,
}
