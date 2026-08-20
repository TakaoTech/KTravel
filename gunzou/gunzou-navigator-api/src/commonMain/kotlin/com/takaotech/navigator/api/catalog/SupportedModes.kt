package com.takaotech.navigator.api.catalog

import com.takaotech.navigator.api.common.TransitMode
import com.takaotech.navigator.api.here.HereTransportMode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * What may be asked of one profile, in the vocabulary of the API that answers it.
 *
 * A single list of modes cannot carry this. The vocabularies of the two HERE endpoints collide by
 * name and not by meaning — road `bus` is a coach the traveller drives, transit `bus` is a scheduled
 * service; road `pedestrian` is a route you ask for, transit walking is only how an answer describes
 * the steps between stops — so a profile declares its modes as one of these variants and a reader
 * that does not recognize the variant knows it cannot interpret the names either.
 *
 * The coarse [com.takaotech.navigator.api.common.TravelMode] is deliberately absent: it describes
 * how a *section of an answer* is travelled, which is a different question from what a *request* may
 * ask for, and answering the second with the first is what put `TRANSIT` in the list of things a
 * caller can select.
 *
 * @property names The modes as plain names, for a client that only needs to count them or list them
 *   without deciding what they mean.
 */
@Serializable
sealed interface SupportedModes {

    val names: List<String>

    /**
     * A profile that takes no mode at all.
     *
     * The default, so a profile added by a newer server than the client reading it still decodes.
     */
    @Serializable
    @SerialName("none")
    data object None : SupportedModes {
        override val names: List<String> get() = emptyList()
    }

    /**
     * The HERE Routing v8 vocabulary: one vehicle per request, since `transportMode` is required and
     * single valued.
     */
    @Serializable
    @SerialName("hereRouting")
    data class HereRouting(@SerialName("modes") val modes: List<HereTransportMode>) : SupportedModes {
        override val names: List<String> get() = modes.map { it.name }
    }

    /**
     * The public transport vocabulary, shared by HERE Public Transit v8, GTFS and OpenTripPlanner: a
     * *filter* over the vehicles an answer may use rather than a choice, and an empty one admits all
     * of them.
     */
    @Serializable
    @SerialName("transit")
    data class Transit(@SerialName("modes") val modes: List<TransitMode>) : SupportedModes {
        override val names: List<String> get() = modes.map { it.name }
    }
}
