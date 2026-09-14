package com.takaotech.gunzou.api.common

import com.takaotech.gunzou.api.common.RoutingProviderId.Companion.from
import com.takaotech.gunzou.api.common.SearchProviderId.Companion.from
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.jvm.JvmInline

/**
 * Identifies the engine that produced a response, grouped by the service it provides.
 *
 * Each service has its own sealed hierarchy — [RoutingProviderId] for routing and transit,
 * [SearchProviderId] for search — so a routing descriptor cannot be handed a search engine, and a
 * `when` over the engines of one service is exhaustive without naming the engines of the other.
 * The same engine can appear in both, as HERE does: the two ids are different values that share
 * the same [value] on the wire.
 *
 * On the wire an id is a plain string. Every hierarchy carries an `Unknown` case on purpose: a
 * server that gains a provider must not break a client built before it existed, and an id this
 * build does not know is a value the client can ignore rather than a decoding failure.
 *
 * @property value The id as it travels on the wire.
 */
sealed interface ProviderId {
    val value: String
}

/**
 * An engine that computes routes, on the road or on a public transport timetable.
 *
 * @property value The id as it travels on the wire.
 */
@Serializable(with = RoutingProviderId.Serializer::class)
sealed class RoutingProviderId : ProviderId {

    final override fun toString(): String = value

    /** HERE, through its Routing v8 and Public Transit v8 APIs. */
    data object Here : RoutingProviderId() {
        override val value: String = "here"
    }

    /** Valhalla, self hosted. */
    data object Valhalla : RoutingProviderId() {
        override val value: String = "valhalla"
    }

    /** OSRM, self hosted. */
    data object Osrm : RoutingProviderId() {
        override val value: String = "osrm"
    }

    /** OpenTripPlanner, self hosted. */
    data object OpenTripPlanner : RoutingProviderId() {
        override val value: String = "otp"
    }

    /**
     * A routing engine this build does not know, kept so that its answers still decode.
     *
     * Never built for an id one of the known cases already spells: [from] resolves those first, so
     * two ids with the same [value] are always the same case.
     *
     * @property value The id as it travels on the wire.
     */
    class Unknown internal constructor(override val value: String) : RoutingProviderId() {
        override fun equals(other: Any?): Boolean = other is Unknown && other.value == value

        override fun hashCode(): Int = value.hashCode()
    }

    /** The lookup from the wire spelling. */
    companion object {

        /**
         * Returns the routing engine spelled [value], or [Unknown] when this build does not know it.
         *
         * @param value The id as it travels on the wire.
         */
        fun from(value: String): RoutingProviderId = when (value) {
            Here.value -> Here
            Valhalla.value -> Valhalla
            Osrm.value -> Osrm
            OpenTripPlanner.value -> OpenTripPlanner
            else -> Unknown(value)
        }
    }

    internal object Serializer : KSerializer<RoutingProviderId> by providerIdSerializer(
        serialName = "com.takaotech.gunzou.api.common.RoutingProviderId",
        from = ::from,
    )
}

/**
 * An engine that finds places from what a user types.
 *
 * @property value The id as it travels on the wire.
 */
@Serializable(with = SearchProviderId.Serializer::class)
sealed class SearchProviderId : ProviderId {

    final override fun toString(): String = value

    /** HERE, through its Geocoding and Search v7 API. */
    data object Here : SearchProviderId() {
        override val value: String = "here"
    }

    /** Photon, the OpenStreetMap geocoder by komoot, self hosted or public. */
    data object Photon : SearchProviderId() {
        override val value: String = "photon"
    }

    /**
     * A search engine this build does not know, kept so that its answers still decode.
     *
     * Never built for an id one of the known cases already spells: [from] resolves those first, so
     * two ids with the same [value] are always the same case.
     *
     * @property value The id as it travels on the wire.
     */
    class Unknown internal constructor(override val value: String) : SearchProviderId() {
        override fun equals(other: Any?): Boolean = other is Unknown && other.value == value

        override fun hashCode(): Int = value.hashCode()
    }

    /** The lookup from the wire spelling. */
    companion object {

        /**
         * Returns the search engine spelled [value], or [Unknown] when this build does not know it.
         *
         * @param value The id as it travels on the wire.
         */
        fun from(value: String): SearchProviderId = when (value) {
            Here.value -> Here
            Photon.value -> Photon
            else -> Unknown(value)
        }
    }

    internal object Serializer : KSerializer<SearchProviderId> by providerIdSerializer(
        serialName = "com.takaotech.gunzou.api.common.SearchProviderId",
        from = ::from,
    )
}

/** Encodes a [ProviderId] as its bare [ProviderId.value], and decodes one through [from]. */
private fun <T : ProviderId> providerIdSerializer(serialName: String, from: (String) -> T): KSerializer<T> =
    object : KSerializer<T> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(serialName, PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: T) = encoder.encodeString(value.value)

        override fun deserialize(decoder: Decoder): T = from(decoder.decodeString())
    }

/**
 * Identifies one profile of a provider, that is one distinct upstream API rather than one mode of
 * transport: HERE road routing and HERE public transit are two profiles, while a car and a bicycle
 * route are the same profile with a different field.
 *
 * A string rather than a sealed hierarchy: a server that gains a profile must not break a client
 * built before it existed, for the same reason as [ProviderId].
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
        val ROUTING = ProviderProfile("routing")

        /** Public transport, on a timetable. */
        val TRANSIT = ProviderProfile("transit")
    }
}
