package com.takaotech.gunzou.here.search.common.category

/**
 * `300` Sights and Museums: The Sights and Museums category is a top level category for places of
 * special interest, such as common tourist attractions, museums and places of worship.
 */
object SightsAndMuseums : PlaceCategoryGroup("300", "Sights and Museums") {
    /** `300-3000` Landmark-Attraction: A designated area of special interest to tourists. */
    object LandmarkAttraction : PlaceCategorySubgroup("300-3000", "Landmark-Attraction") {
        override val group: PlaceCategoryGroup get() = SightsAndMuseums

        /**
         * `300-3000-0000` Landmark-Attraction: A designated area of special interest to tourists.
         * This is a base-level category that should be used for all places that do not fit other
         * categories defined for Landmark-Attraction (300-3000-xxxx).
         */
        val LANDMARK_ATTRACTION: PlaceCategoryLeaf = leaf("300-3000-0000", "Landmark-Attraction")

        /**
         * `300-3000-0023` Tourist Attraction: A landmark or attraction that is regionally known, is
         * of special interest to tourists, and does not meet the criteria for another more specific
         * category. Examples of tourist attractions include the Eiffel Tower or Golden Gate Bridge.
         */
        val TOURIST_ATTRACTION: PlaceCategoryLeaf = leaf("300-3000-0023", "Tourist Attraction")

        /** `300-3000-0024` Gallery: A landmark or attraction that provides works of art. */
        val GALLERY: PlaceCategoryLeaf = leaf("300-3000-0024", "Gallery")

        /**
         * `300-3000-0025` Historical Monument: A landmark or attraction with important historical
         * or cultural value. For example, Machu Picchu, the Pyramids at Giza, Stonehenge, the
         * Colosseum, Parthenon and Taj Mahal.
         */
        val HISTORICAL_MONUMENT: PlaceCategoryLeaf = leaf("300-3000-0025", "Historical Monument")

        /**
         * `300-3000-0030` Castle: A building (typically from the Medieval era) designed with a
         * fortified exterior wall to protect royalty or nobility from attack. Castles were often
         * surrounded by a moat (a deep trench usually filled with water).
         */
        val CASTLE: PlaceCategoryLeaf = leaf("300-3000-0030", "Castle")

        /**
         * `300-3000-0065` Winery: A landmark or attraction that makes or provides wine. Some places
         * may also provide public tours or wine tastings.
         */
        val WINERY: PlaceCategoryLeaf = leaf("300-3000-0065", "Winery")

        /**
         * `300-3000-0350` Brewery: An establishment where beer is produced. This includes
         * conglomerate breweries and microbreweries.
         */
        val BREWERY: PlaceCategoryLeaf = leaf("300-3000-0350", "Brewery")

        /**
         * `300-3000-0351` Distillery: A establishment where the distillation of alcohol takes
         * place. Some places offer public or private tours, and products for sale.
         */
        val DISTILLERY: PlaceCategoryLeaf = leaf("300-3000-0351", "Distillery")

        /**
         * `300-3000-0450` Hot Spring: A natural warm spring that has a bathing facility located
         * outdoors and/or indoors. Note: This category is a Japan-only category.
         */
        val HOT_SPRING: PlaceCategoryLeaf = leaf("300-3000-0450", "Hot Spring")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                LANDMARK_ATTRACTION,
                TOURIST_ATTRACTION,
                GALLERY,
                HISTORICAL_MONUMENT,
                CASTLE,
                WINERY,
                BREWERY,
                DISTILLERY,
                HOT_SPRING,
            )
        }
    }

    /**
     * `300-3100` Museum: An establishment dedicated to the preservation and exhibition of artistic,
     * historical, or scientific artifacts.
     */
    object Museum : PlaceCategorySubgroup("300-3100", "Museum") {
        override val group: PlaceCategoryGroup get() = SightsAndMuseums

        /**
         * `300-3100-0000` Museum: An establishment dedicated to the preservation and exhibition of
         * artistic, historical, or scientific artifacts. This is a base-level category that should
         * be used for all places that do not fit other categories defined for Museum
         * (300-3100-xxxx).
         */
        val MUSEUM: PlaceCategoryLeaf = leaf("300-3100-0000", "Museum")

        /**
         * `300-3100-0026` Science Museum: A museum dedicated to showing artifacts and exhibits of
         * scientific interest.
         */
        val SCIENCE_MUSEUM: PlaceCategoryLeaf = leaf("300-3100-0026", "Science Museum")

        /**
         * `300-3100-0027` Children's Museum: A museum dedicated to showing artifacts and exhibits
         * of interest to children.
         */
        val CHILDREN_S_MUSEUM: PlaceCategoryLeaf = leaf("300-3100-0027", "Children's Museum")

        /**
         * `300-3100-0028` History Museum: A museum dedicated to showing artifacts and exhibits of
         * historical nature.
         */
        val HISTORY_MUSEUM: PlaceCategoryLeaf = leaf("300-3100-0028", "History Museum")

        /**
         * `300-3100-0029` Art Museum: A museum dedicated to showing artifacts and exhibits of art.
         */
        val ART_MUSEUM: PlaceCategoryLeaf = leaf("300-3100-0029", "Art Museum")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                MUSEUM,
                SCIENCE_MUSEUM,
                CHILDREN_S_MUSEUM,
                HISTORY_MUSEUM,
                ART_MUSEUM,
            )
        }
    }

    /**
     * `300-3200` Religious Place: An establishment special religious significance or where
     * religious services are held.
     */
    object ReligiousPlace : PlaceCategorySubgroup("300-3200", "Religious Place") {
        override val group: PlaceCategoryGroup get() = SightsAndMuseums

        /**
         * `300-3200-0000` Religious Place: An establishment of special religious significance or
         * where religious services are held. This is a base-level category that should be used for
         * all places that do not fit other categories defined for Religious Place (300-3200-xxxx).
         */
        val RELIGIOUS_PLACE: PlaceCategoryLeaf = leaf("300-3200-0000", "Religious Place")

        /** `300-3200-0030` Church: A religious place of worship for the Christian faith. */
        val CHURCH: PlaceCategoryLeaf = leaf("300-3200-0030", "Church")

        /**
         * `300-3200-0031` Temple: A religious place of worship dedicated to religious or spiritual
         * activities. Places in this category may apply to Hinduism, Buddhism, Taoism, Sikh, Jain,
         * Ayyavazhi and other faiths.
         */
        val TEMPLE: PlaceCategoryLeaf = leaf("300-3200-0031", "Temple")

        /** `300-3200-0032` Synagogue: A religious place of worship for the Jewish faith. */
        val SYNAGOGUE: PlaceCategoryLeaf = leaf("300-3200-0032", "Synagogue")

        /**
         * `300-3200-0033` Ashram: A hermitage, monastic community, or other place of religious
         * retreat for Hindus.
         */
        val ASHRAM: PlaceCategoryLeaf = leaf("300-3200-0033", "Ashram")

        /** `300-3200-0034` Mosque: A religious place of worship for the Islamic faith. */
        val MOSQUE: PlaceCategoryLeaf = leaf("300-3200-0034", "Mosque")

        /**
         * `300-3200-0035` Shrine: A religious place of worship which is consecrated for the
         * reverence or worship of a person or thing. Many religions have shrines, and these may be
         * found in random locations such as inside of buildings, homes, shops, or tiny outdoor
         * shelters.
         */
        val SHRINE: PlaceCategoryLeaf = leaf("300-3200-0035", "Shrine")

        /**
         * `300-3200-0309` Other Place of Worship: A Place of Worship that does not fit into other
         * defined Religious Place categories (300-3200-xxxx).
         */
        val OTHER_PLACE_OF_WORSHIP: PlaceCategoryLeaf =
            leaf("300-3200-0309", "Other Place of Worship")

        /** `300-3200-0375` Gurdwara: A religious place of worship for the Sikh faith. */
        val GURDWARA: PlaceCategoryLeaf = leaf("300-3200-0375", "Gurdwara")

        /**
         * `300-3200-0376` Pagoda: A religious place of worship most commonly serving Buddhist,
         * Hindu, or Taoist religious activities. It is a tower with many levels which usually
         * consists of eaves that curve upward.
         */
        val PAGODA: PlaceCategoryLeaf = leaf("300-3200-0376", "Pagoda")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                RELIGIOUS_PLACE,
                CHURCH,
                TEMPLE,
                SYNAGOGUE,
                ASHRAM,
                MOSQUE,
                SHRINE,
                OTHER_PLACE_OF_WORSHIP,
                GURDWARA,
                PAGODA,
            )
        }
    }

    override val subgroups: List<PlaceCategorySubgroup> by lazy {
        listOf(
            LandmarkAttraction,
            Museum,
            ReligiousPlace,
        )
    }
}
