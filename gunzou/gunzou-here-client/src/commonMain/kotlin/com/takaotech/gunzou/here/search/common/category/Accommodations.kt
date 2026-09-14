package com.takaotech.gunzou.here.search.common.category

/**
 * `500` Accommodations: The Accommodations category is a top level category for places offering
 * lodging accommodations, dwellings or similar living quarters to travellers, such as hotels,
 * motels, resorts, cruise ships and campgrounds.
 */
object Accommodations : PlaceCategoryGroup("500", "Accommodations") {
    /** `500-5000` Hotel-Motel: A business that provides lodging or temporary living quarters. */
    object HotelMotel : PlaceCategorySubgroup("500-5000", "Hotel-Motel") {
        override val group: PlaceCategoryGroup get() = Accommodations

        /**
         * `500-5000-0000` Hotel or Motel: A business that provides lodging or temporary living
         * quarters. This is a base-level category that should be used for all places that do not
         * fit other categories defined for Hotel-Motel (500-5000-xxxx).
         */
        val HOTEL_OR_MOTEL: PlaceCategoryLeaf = leaf("500-5000-0000", "Hotel or Motel")

        /**
         * `500-5000-0053` Hotel: An establishment that provides short-term or extended stay
         * lodging. Places in this category may also provide other services and amenities, such as
         * meals, entertainment, dry cleaning and various other personal services.
         */
        val HOTEL: PlaceCategoryLeaf = leaf("500-5000-0053", "Hotel")

        /**
         * `500-5000-0054` Motel: An establishment that provides lodging to the public. These places
         * usually have rooms that are accessible from a parking area.
         */
        val MOTEL: PlaceCategoryLeaf = leaf("500-5000-0054", "Motel")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                HOTEL_OR_MOTEL,
                HOTEL,
                MOTEL,
            )
        }
    }

    /**
     * `500-5100` Lodging: A business that provides lodging to the public generally without room
     * service.
     */
    object Lodging : PlaceCategorySubgroup("500-5100", "Lodging") {
        override val group: PlaceCategoryGroup get() = Accommodations

        /**
         * `500-5100-0000` Lodging: A business that provides lodging to the public generally without
         * room service. This is a base-level category that should be used for all places that do
         * not fit other categories defined for Lodging (500-5100-xxxx).
         */
        val LODGING: PlaceCategoryLeaf = leaf("500-5100-0000", "Lodging")

        /**
         * `500-5100-0055` Hostel: An establishment that provides an inexpensive lodging place for
         * travelers. Lodging arrangements are usually in dormitory settings where a guest can rent
         * a bed or bunk bed. Guests often share amenities with other guests.
         */
        val HOSTEL: PlaceCategoryLeaf = leaf("500-5100-0055", "Hostel")

        /**
         * `500-5100-0056` Campground: An establishment that provides a designated area for
         * short-term camping. These places generally serve tent, trailer or recreation vehicle (RV)
         * camping.
         */
        val CAMPGROUND: PlaceCategoryLeaf = leaf("500-5100-0056", "Campground")

        /**
         * `500-5100-0057` Guest House: An establishment that provides lodging in an adjacent
         * building next to the main structure. Guest houses are generally a small house or cottage.
         */
        val GUEST_HOUSE: PlaceCategoryLeaf = leaf("500-5100-0057", "Guest House")

        /**
         * `500-5100-0058` Bed and Breakfast: An establishment that provides lodging and breakfast
         * at an inclusive price. Bed and Breakfasts are typically private homes that contain a
         * limited number of bedrooms.
         */
        val BED_AND_BREAKFAST: PlaceCategoryLeaf = leaf("500-5100-0058", "Bed and Breakfast")

        /**
         * `500-5100-0059` Holiday Park: An establishment that provides rental cottages or rooms.
         * Resorts generally provide various recreational facilities, such as bowling, restaurants,
         * swimming or mini golf.
         */
        val HOLIDAY_PARK: PlaceCategoryLeaf = leaf("500-5100-0059", "Holiday Park")

        /**
         * `500-5100-0060` Short-Time Motel: An establishment that provides lodging designed for
         * romantic encounters, or short rests, but usually not requiring an overnight stay.
         */
        val SHORT_TIME_MOTEL: PlaceCategoryLeaf = leaf("500-5100-0060", "Short-Time Motel")

        /**
         * `500-5100-0061` Ryokan: An establishment that provides lodging with a traditional
         * Japanese-style setting. Ryokans commonly contain matted flooring, sliding doors, shared
         * rooms/baths, and serve traditional Japanese food.
         */
        val RYOKAN: PlaceCategoryLeaf = leaf("500-5100-0061", "Ryokan")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                LODGING,
                HOSTEL,
                CAMPGROUND,
                GUEST_HOUSE,
                BED_AND_BREAKFAST,
                HOLIDAY_PARK,
                SHORT_TIME_MOTEL,
                RYOKAN,
            )
        }
    }

    override val subgroups: List<PlaceCategorySubgroup> by lazy {
        listOf(
            HotelMotel,
            Lodging,
        )
    }
}
