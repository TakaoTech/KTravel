package com.takaotech.gunzou.api.search

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * The broad kind of a place: what a map would pick an icon for.
 *
 * The groups are the level 1 categories of the HERE Places Category System. They are coarse on
 * purpose: a provider neutral contract cannot carry one provider's finest categories, but every
 * provider with points of interest can say which of these a place belongs to.
 *
 * A string rather than an enum, so a group this client does not know still decodes.
 *
 * @property value The group as it travels on the wire.
 */
// TODO revisit the groups when Photon is implemented, mapping its `osm_key`/`osm_value` onto them.
@Serializable
@JvmInline
value class PlaceCategoryGroup(val value: String) {
    override fun toString(): String = value

    /** The groups this contract knows, in the order HERE documents them. */
    companion object {
        /** Restaurants, cafés, bars. */
        val EAT_AND_DRINK = PlaceCategoryGroup("eatAndDrink")

        /** Nightlife, cinemas, theatres, casinos. */
        val GOING_OUT_ENTERTAINMENT = PlaceCategoryGroup("goingOutEntertainment")

        /** Landmarks, monuments, museums, religious places. */
        val SIGHTS_AND_MUSEUMS = PlaceCategoryGroup("sightsAndMuseums")

        /** Mountains, lakes, beaches, forests. */
        val NATURAL_AND_GEOGRAPHICAL = PlaceCategoryGroup("naturalAndGeographical")

        /** Airports, stations, stops, parking, fuel. */
        val TRANSPORT = PlaceCategoryGroup("transport")

        /** Hotels, hostels, campsites. */
        val ACCOMMODATIONS = PlaceCategoryGroup("accommodations")

        /** Parks, sports facilities, recreation. */
        val LEISURE_AND_OUTDOOR = PlaceCategoryGroup("leisureAndOutdoor")

        /** Shops of every kind. */
        val SHOPPING = PlaceCategoryGroup("shopping")

        /** Banks, offices, repairs, services. */
        val BUSINESS_AND_SERVICES = PlaceCategoryGroup("businessAndServices")

        /** Hospitals, schools, government, public facilities. */
        val FACILITIES = PlaceCategoryGroup("facilities")

        /** Buildings, neighbourhoods, industrial and residential areas. */
        val AREAS_AND_BUILDINGS = PlaceCategoryGroup("areasAndBuildings")
    }
}
