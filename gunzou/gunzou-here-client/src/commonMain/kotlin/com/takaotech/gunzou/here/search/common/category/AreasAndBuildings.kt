package com.takaotech.gunzou.here.search.common.category

/**
 * `900` Areas and Buildings: The Areas and Buildings category is a top level category for places
 * that are owned, operated or managed by municipalities, such as cities, towns, villages, boroughs
 * and shires.
 */
object AreasAndBuildings : PlaceCategoryGroup("900", "Areas and Buildings") {
    /**
     * `900-9100` City, Town or Village: Represents a named settlement that may be a (large) city,
     * town, or tiny village.
     */
    object CityTownOrVillage : PlaceCategorySubgroup("900-9100", "City, Town or Village") {
        override val group: PlaceCategoryGroup get() = AreasAndBuildings

        /**
         * `900-9100-0000` City, Town or Village: Represents a named settlement that may be a
         * (large) city, town, or tiny village. This is a base-level category that should be used
         * for all places that do not fit other categories defined for City, Town or Village
         * (900-9100-xxxx).
         */
        val CITY_TOWN_OR_VILLAGE: PlaceCategoryLeaf = leaf("900-9100-0000", "City, Town or Village")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                CITY_TOWN_OR_VILLAGE,
            )
        }
    }

    /**
     * `900-9200` Outdoor Area-Complex: Outdoor areas or complexes with designations for specific
     * businesses or interests.
     */
    object OutdoorAreaComplex : PlaceCategorySubgroup("900-9200", "Outdoor Area-Complex") {
        override val group: PlaceCategoryGroup get() = AreasAndBuildings

        /**
         * `900-9200-0000` Outdoor Area-Complex: An establishment or area located outdoors. This is
         * a base-level category that should be used for all places that do not fit other categories
         * defined for Outdoor Area-Complex (900-9200-xxxx).
         */
        val OUTDOOR_AREA_COMPLEX: PlaceCategoryLeaf = leaf("900-9200-0000", "Outdoor Area-Complex")

        /**
         * `900-9200-0218` Industrial Zone: A non-residential area that is dedicated to
         * manufacturing, industrial activities or storage facilities. This also includes business
         * parks containing service industries.
         */
        val INDUSTRIAL_ZONE: PlaceCategoryLeaf = leaf("900-9200-0218", "Industrial Zone")

        /**
         * `900-9200-0219` Marina: A designated area that offers docking and other services for
         * small boats, yachts, or pleasure craft.
         */
        val MARINA: PlaceCategoryLeaf = leaf("900-9200-0219", "Marina")

        /**
         * `900-9200-0220` RV Parks: A designated area that provides camping facilities for
         * recreational vehicles.
         */
        val RV_PARKS: PlaceCategoryLeaf = leaf("900-9200-0220", "RV Parks")

        /**
         * `900-9200-0299` Collective Community: A facility that provides an area for members to
         * hold a common social, political, religious or spiritual events. For example, kibbutz,
         * dojos, moshavs, agricultural communes, eco villages, art communities, convents,
         * monasteries and housing cooperatives.
         */
        val COLLECTIVE_COMMUNITY: PlaceCategoryLeaf = leaf("900-9200-0299", "Collective Community")

        /**
         * `900-9200-0301` Island: A natural and geographical feature of land that is entirely
         * separated by water from the mainland.
         */
        val ISLAND: PlaceCategoryLeaf = leaf("900-9200-0301", "Island")

        /**
         * `900-9200-0386` Meeting Point: A designated place where people can gather or must report
         * to during an emergency (for example, tsunami, earthquake, fire, and so on).
         */
        val MEETING_POINT: PlaceCategoryLeaf = leaf("900-9200-0386", "Meeting Point")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                OUTDOOR_AREA_COMPLEX,
                INDUSTRIAL_ZONE,
                MARINA,
                RV_PARKS,
                COLLECTIVE_COMMUNITY,
                ISLAND,
                MEETING_POINT,
            )
        }
    }

    /** `900-9300` Building: Areas and buildings designated for residential or office use. */
    object Building : PlaceCategorySubgroup("900-9300", "Building") {
        override val group: PlaceCategoryGroup get() = AreasAndBuildings

        /**
         * `900-9300-0000` Building: This is a plot of land that has a street number assigned to it.
         * This is a base-level category that should be used for all places that do not fit other
         * categories defined for Building (900-9300-xxxx).
         */
        val BUILDING: PlaceCategoryLeaf = leaf("900-9300-0000", "Building")

        /**
         * `900-9300-0221` Residential Area-Building: A building designated specifically for
         * residential use.
         */
        val RESIDENTIAL_AREA_BUILDING: PlaceCategoryLeaf =
            leaf("900-9300-0221", "Residential Area-Building")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                BUILDING,
                RESIDENTIAL_AREA_BUILDING,
            )
        }
    }

    /**
     * `900-9400` Administrative Region-Streets: An administrative region, such as a postal area, or
     * a named street/square/intersection.
     */
    object AdministrativeRegionStreets :
        PlaceCategorySubgroup("900-9400", "Administrative Region-Streets") {
        override val group: PlaceCategoryGroup get() = AreasAndBuildings

        /**
         * `900-9400-0000` Administrative Region-Streets: This is an administrative region, such as
         * a postal area, or a named street/square/intersection. This is a base-level category that
         * should be used for all places that do not fit other categories defined for Administrative
         * Region-Streets (900-9400-xxxx).
         */
        val ADMINISTRATIVE_REGION_STREETS: PlaceCategoryLeaf =
            leaf("900-9400-0000", "Administrative Region-Streets")

        /**
         * `900-9400-0399` Administrative Region: This is either a country, a state / province, a
         * county or an even smaller area like a district or sub-district. Named areas like the
         * Boston Metro Area also fall into this category.
         */
        val ADMINISTRATIVE_REGION: PlaceCategoryLeaf =
            leaf("900-9400-0399", "Administrative Region")

        /**
         * `900-9400-0400` Postal Area: A postal area in a country. This could also be a group of
         * postal areas, for example in Canada this could be "M4B 1L5" or "M4B 1L6," as well as
         * "M4B" or "M4B 1.".
         */
        val POSTAL_AREA: PlaceCategoryLeaf = leaf("900-9400-0400", "Postal Area")

        /**
         * `900-9400-0401` Street or Square: A named street or square. This could be anything from a
         * small pedestrian passage to a big highway. Large named squares fall into this category.
         */
        val STREET_OR_SQUARE: PlaceCategoryLeaf = leaf("900-9400-0401", "Street or Square")

        /**
         * `900-9400-0402` Intersection: A road intersection. It is typically represented by two or
         * more streets meeting at the intersection. It could also be a named intersection.
         */
        val INTERSECTION: PlaceCategoryLeaf = leaf("900-9400-0402", "Intersection")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                ADMINISTRATIVE_REGION_STREETS,
                ADMINISTRATIVE_REGION,
                POSTAL_AREA,
                STREET_OR_SQUARE,
                INTERSECTION,
            )
        }
    }

    override val subgroups: List<PlaceCategorySubgroup> by lazy {
        listOf(
            CityTownOrVillage,
            OutdoorAreaComplex,
            Building,
            AdministrativeRegionStreets,
        )
    }
}
