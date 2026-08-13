package com.takaotech.navigator.api.common

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * Identifies the routing engine that produced a response.
 *
 * A string and not an enum on purpose: a server that gains a provider must not break a client built
 * before it existed, and an unknown enum constant is a decoding failure rather than a value the
 * client can ignore. The known ids live as constants in the companion.
 *
 * @property value The id as it travels on the wire.
 */
@Serializable
@JvmInline
value class ProviderId(val value: String) {
    override fun toString(): String = value

    /** The engines this contract has been designed against. */
    companion object {
        /** HERE, the only engine implemented today. */
        val HERE = ProviderId("here")

        /** Valhalla, self hosted. */
        val VALHALLA = ProviderId("valhalla")

        /** OSRM, self hosted. */
        val OSRM = ProviderId("osrm")

        /** OpenTripPlanner, self hosted. */
        val OPEN_TRIP_PLANNER = ProviderId("otp")
    }
}

/**
 * Identifies one profile of a provider, that is one distinct upstream API rather than one mode of
 * transport: HERE road routing and HERE public transit are two profiles, while a car and a bicycle
 * route are the same profile with a different field.
 *
 * A string for the same forward compatibility reason as [ProviderId].
 *
 * @property value The profile name as it travels on the wire.
 */
@Serializable
@JvmInline
value class ProviderProfile(val value: String) {
    override fun toString(): String = value

    /** The profiles this contract currently serves. */
    companion object {
        /** Road routing, whatever the vehicle. */
        val CAR = ProviderProfile("car")

        /** Public transport, on a timetable. */
        val TRANSIT = ProviderProfile("transit")
    }
}
