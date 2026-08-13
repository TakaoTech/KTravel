package com.takaotech.ktravel.endpoint.here

import com.takaotech.navigation.publictransit.model.TransitModeType
import com.takaotech.navigator.api.common.TransitMode

/**
 * The one table saying which contract mode is which HERE mode, read in both directions.
 *
 * A table and not two `when` expressions: written as branches, the two directions are fifteen lines
 * each that have to stay each other's mirror image, and nothing catches it when one gains an entry
 * the other does not. Here adding a mode is a single line, and it is impossible to add it to one
 * direction only.
 *
 * [TransitMode.OTHER] is deliberately absent. It exists so an unknown mode in a response still
 * decodes, and it has no upstream counterpart to be sent as.
 */
private val CONTRACT_TO_HERE: Map<TransitMode, TransitModeType> = mapOf(
    TransitMode.HIGH_SPEED_TRAIN to TransitModeType.HIGH_SPEED_TRAIN,
    TransitMode.INTERCITY_TRAIN to TransitModeType.INTERCITY_TRAIN,
    TransitMode.INTER_REGIONAL_TRAIN to TransitModeType.INTER_REGIONAL_TRAIN,
    TransitMode.REGIONAL_TRAIN to TransitModeType.REGIONAL_TRAIN,
    TransitMode.CITY_TRAIN to TransitModeType.CITY_TRAIN,
    TransitMode.BUS to TransitModeType.BUS,
    TransitMode.PRIVATE_BUS to TransitModeType.PRIVATE_BUS,
    TransitMode.BUS_RAPID to TransitModeType.BUS_RAPID,
    TransitMode.FERRY to TransitModeType.FERRY,
    TransitMode.SUBWAY to TransitModeType.SUBWAY,
    TransitMode.LIGHT_RAIL to TransitModeType.LIGHT_RAIL,
    TransitMode.MONORAIL to TransitModeType.MONORAIL,
    TransitMode.INCLINED to TransitModeType.INCLINED,
    TransitMode.AERIAL to TransitModeType.AERIAL,
    TransitMode.FLIGHT to TransitModeType.FLIGHT,
)

/** Keyed by the string HERE writes in a response, which is what a payload actually carries. */
private val HERE_VALUE_TO_CONTRACT: Map<String, TransitMode> =
    CONTRACT_TO_HERE.entries.associate { (contract, here) -> here.value to contract }

/**
 * The HERE mode to filter on, or null for one this contract cannot ask about.
 */
internal fun TransitMode.toHereOrNull(): TransitModeType? = CONTRACT_TO_HERE[this]

/**
 * The kind of vehicle, in the contract's vocabulary.
 *
 * An unrecognised mode becomes [TransitMode.OTHER] rather than a failure: HERE adds modes, and a
 * journey the client can still draw is worth more than a 502 because a ferry was called something
 * new.
 */
internal fun String?.toTransitMode(): TransitMode = HERE_VALUE_TO_CONTRACT[this] ?: TransitMode.OTHER
