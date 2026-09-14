package com.takaotech.gunzou.here.search.common.category

/**
 * `350` Natural and Geographical: The Natural and Geographical category is a top level category for
 * natural or man-made areas of regional importance, such as bodies of water, mountains, forested
 * areas and other geographic areas.
 */
object NaturalAndGeographical : PlaceCategoryGroup("350", "Natural and Geographical") {
    /**
     * `350-3500` Body of Water: A natural and geographical feature of the earth's surface that is
     * covered with water, such as a lake, river, stream or ocean.
     */
    object BodyOfWater : PlaceCategorySubgroup("350-3500", "Body of Water") {
        override val group: PlaceCategoryGroup get() = NaturalAndGeographical

        /**
         * `350-3500-0233` Body of Water: A natural and geographical feature of the earth's surface
         * that is covered with water, such as a lake, river, stream or ocean.
         */
        val BODY_OF_WATER: PlaceCategoryLeaf = leaf("350-3500-0233", "Body of Water")

        /**
         * `350-3500-0234` Reservoir: A body of water, such as a man-made pond or lake, that is used
         * for the retention and regulation of water.
         */
        val RESERVOIR: PlaceCategoryLeaf = leaf("350-3500-0234", "Reservoir")

        /**
         * `350-3500-0235` Waterfall: A body of water where a flow of water, such as a mountain
         * stream or river, falls over a steep incline or vertical drop.
         */
        val WATERFALL: PlaceCategoryLeaf = leaf("350-3500-0235", "Waterfall")

        /** `350-3500-0300` Bay-Harbor: A coastal body of water where ships can safely anchor. */
        val BAY_HARBOR: PlaceCategoryLeaf = leaf("350-3500-0300", "Bay-Harbor")

        /**
         * `350-3500-0302` River: A body of water, usually containing fresh water, that generally
         * flows through land and empties into an ocean, lake, river or other body of water.
         */
        val RIVER: PlaceCategoryLeaf = leaf("350-3500-0302", "River")

        /**
         * `350-3500-0303` Canal: An artificial waterway constructed for shipping, irrigation or
         * recreational usage.
         */
        val CANAL: PlaceCategoryLeaf = leaf("350-3500-0303", "Canal")

        /**
         * `350-3500-0304` Lake: A large body of water that may contain fresh water or salt water. A
         * lake is larger than a pond and must be regionally recognized. Lakes are generally
         * separated from the ocean by land.
         */
        val LAKE: PlaceCategoryLeaf = leaf("350-3500-0304", "Lake")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                BODY_OF_WATER,
                RESERVOIR,
                WATERFALL,
                BAY_HARBOR,
                RIVER,
                CANAL,
                LAKE,
            )
        }
    }

    /**
     * `350-3510` Mountain or Hill: A natural and geographical feature that is higher than the
     * surrounding land.
     */
    object MountainOrHill : PlaceCategorySubgroup("350-3510", "Mountain or Hill") {
        override val group: PlaceCategoryGroup get() = NaturalAndGeographical

        /**
         * `350-3510-0236` Mountain or Hill: A natural and geographical feature that is higher than
         * the surrounding land. Places in this category must be regionally recognized.
         */
        val MOUNTAIN_OR_HILL: PlaceCategoryLeaf = leaf("350-3510-0236", "Mountain or Hill")

        /**
         * `350-3510-0237` Mountain Passes: A route through a mountain range or over a ridge.
         * Mountain passes must be regionally recognized.
         */
        val MOUNTAIN_PASSES: PlaceCategoryLeaf = leaf("350-3510-0237", "Mountain Passes")

        /**
         * `350-3510-0238` Mountain Peaks: A summit or top of a mountain. Mountain peaks must be
         * regionally recognized.
         */
        val MOUNTAIN_PEAKS: PlaceCategoryLeaf = leaf("350-3510-0238", "Mountain Peaks")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                MOUNTAIN_OR_HILL,
                MOUNTAIN_PASSES,
                MOUNTAIN_PEAKS,
            )
        }
    }

    /** `350-3520` Undersea Feature: A natural or artificial feature that is below sea level. */
    object UnderseaFeature : PlaceCategorySubgroup("350-3520", "Undersea Feature") {
        override val group: PlaceCategoryGroup get() = NaturalAndGeographical

        /**
         * `350-3520-0224` Undersea Feature: A natural or artificial feature that is below sea
         * level. For example, coral reef barrier, ship wreck and more. Places in this category must
         * be regionally recognized.
         */
        val UNDERSEA_FEATURE: PlaceCategoryLeaf = leaf("350-3520-0224", "Undersea Feature")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                UNDERSEA_FEATURE,
            )
        }
    }

    /**
     * `350-3522` Forest, Heath or Other Vegetation: A dense growth of trees, open uncultivated land
     * or other large masses of vegetation.
     */
    object ForestHeathOrOtherVegetation :
        PlaceCategorySubgroup("350-3522", "Forest, Heath or Other Vegetation") {
        override val group: PlaceCategoryGroup get() = NaturalAndGeographical

        /**
         * `350-3522-0239` Forest, Heath or Other Vegetation: A dense growth of trees, open
         * uncultivated land or other large masses of vegetation.
         */
        val FOREST_HEATH_OR_OTHER_VEGETATION: PlaceCategoryLeaf =
            leaf("350-3522-0239", "Forest, Heath or Other Vegetation")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                FOREST_HEATH_OR_OTHER_VEGETATION,
            )
        }
    }

    /**
     * `350-3550` Natural and Geographical: A feature not classified as other natural or geographic
     * categories. Named Natural and Geographical by HERE, renamed here so that it does not shadow
     * its group.
     */
    object OtherNaturalAndGeographical :
        PlaceCategorySubgroup("350-3550", "Natural and Geographical") {
        override val group: PlaceCategoryGroup get() = NaturalAndGeographical

        /**
         * `350-3550-0336` Natural and Geographical: A feature not classified as a Body of Water
         * (350-3500), Mountain or Hill (350-3510), Undersea Feature (350-3520), or Forest, Heath or
         * Other Vegetation (350-3522).
         */
        val NATURAL_AND_GEOGRAPHICAL: PlaceCategoryLeaf =
            leaf("350-3550-0336", "Natural and Geographical")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                NATURAL_AND_GEOGRAPHICAL,
            )
        }
    }

    override val subgroups: List<PlaceCategorySubgroup> by lazy {
        listOf(
            BodyOfWater,
            MountainOrHill,
            UnderseaFeature,
            ForestHeathOrOtherVegetation,
            OtherNaturalAndGeographical,
        )
    }
}
