package com.takaotech.gunzou.here.search.common.category

/**
 * `550` Leisure and Outdoor: The Leisure and Outdoor category is a top level category for places
 * that are designated for sports, recreation, parking, beaches and other leisure and outdoor
 * activities.
 */
object LeisureAndOutdoor : PlaceCategoryGroup("550", "Leisure and Outdoor") {
    /** `550-5510` Outdoor-Recreation: Public land preserved and maintained for recreational use. */
    object OutdoorRecreation : PlaceCategorySubgroup("550-5510", "Outdoor-Recreation") {
        override val group: PlaceCategoryGroup get() = LeisureAndOutdoor

        /**
         * `550-5510-0000` Outdoor-Recreation: Public land preserved and maintained for recreational
         * use. This is a base-level category that should be used for all places that do not fit
         * other categories defined for Park-Recreation (550-5510-xxxx).
         */
        val OUTDOOR_RECREATION: PlaceCategoryLeaf = leaf("550-5510-0000", "Outdoor-Recreation")

        /**
         * `550-5510-0202` Park-Recreation Area: A protected area preserved and maintained for
         * public enjoyment including fairgrounds.
         */
        val PARK_RECREATION_AREA: PlaceCategoryLeaf = leaf("550-5510-0202", "Park-Recreation Area")

        /**
         * `550-5510-0203` Sports Field: A sporting activity area that is accessible to the public.
         */
        val SPORTS_FIELD: PlaceCategoryLeaf = leaf("550-5510-0203", "Sports Field")

        /**
         * `550-5510-0204` Garden: A designated area containing plants, flowers, trees or other
         * vegetation. This includes private and public gardens. Common examples include
         * conservatories and botanical gardens.
         */
        val GARDEN: PlaceCategoryLeaf = leaf("550-5510-0204", "Garden")

        /**
         * `550-5510-0205` Beach: A coastal area preserved and maintained for public enjoyment that
         * is adjacent to lakes, oceans, rivers and connecting bay/harbors.
         */
        val BEACH: PlaceCategoryLeaf = leaf("550-5510-0205", "Beach")

        /**
         * `550-5510-0206` Recreation Center: A designated area that is open to the public for the
         * purposes of social, educational, or cultural activities, in addition to amateur
         * individual and team sports.
         */
        val RECREATION_CENTER: PlaceCategoryLeaf = leaf("550-5510-0206", "Recreation Center")

        /**
         * `550-5510-0227` Ski Lift: A designated area at a ski resort that provides access to the
         * top of the mountain.
         */
        val SKI_LIFT: PlaceCategoryLeaf = leaf("550-5510-0227", "Ski Lift")

        /**
         * `550-5510-0242` Scenic Point: A designated area providing access to scenic viewpoints.
         * Scenic points generally are located along scenic roadways and have regional significance.
         */
        val SCENIC_POINT: PlaceCategoryLeaf = leaf("550-5510-0242", "Scenic Point")

        /**
         * `550-5510-0358` Off Road Trailhead: A designated point where an off-road vehicle trail
         * begins. The off-road trailhead may contain restrooms, maps, signposts and informational
         * centers.
         */
        val OFF_ROAD_TRAILHEAD: PlaceCategoryLeaf = leaf("550-5510-0358", "Off Road Trailhead")

        /**
         * `550-5510-0359` Trailhead: A designated point where a trail begins. The trailhead may
         * contain restrooms, maps, signposts and informational centers.
         */
        val TRAILHEAD: PlaceCategoryLeaf = leaf("550-5510-0359", "Trailhead")

        /**
         * `550-5510-0374` Off-Road Vehicle Area: A designated area or trail for driving off-road
         * vehicles (4WD, ATVs, etc).
         */
        val OFF_ROAD_VEHICLE_AREA: PlaceCategoryLeaf =
            leaf("550-5510-0374", "Off-Road Vehicle Area")

        /**
         * `550-5510-0378` Campsite: A designated area where people can camp using tents or camper
         * vans for overnight stays in the outdoors.
         */
        val CAMPSITE: PlaceCategoryLeaf = leaf("550-5510-0378", "Campsite")

        /**
         * `550-5510-0379` Outdoor Service: An establishment or designated area that provides
         * amenities specific to outdoor activities.
         */
        val OUTDOOR_SERVICE: PlaceCategoryLeaf = leaf("550-5510-0379", "Outdoor Service")

        /**
         * `550-5510-0380` Ranger Station: A Ranger Station is a building within a park or other
         * outdoor recreation area which is usually public and affiliated with the park where it is
         * located.
         */
        val RANGER_STATION: PlaceCategoryLeaf = leaf("550-5510-0380", "Ranger Station")

        /**
         * `550-5510-0387` Bicycle Service: An outdoor location that offers bicycle repairs and
         * maintenance as a self-service.
         */
        val BICYCLE_SERVICE: PlaceCategoryLeaf = leaf("550-5510-0387", "Bicycle Service")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                OUTDOOR_RECREATION,
                PARK_RECREATION_AREA,
                SPORTS_FIELD,
                GARDEN,
                BEACH,
                RECREATION_CENTER,
                SKI_LIFT,
                SCENIC_POINT,
                OFF_ROAD_TRAILHEAD,
                TRAILHEAD,
                OFF_ROAD_VEHICLE_AREA,
                CAMPSITE,
                OUTDOOR_SERVICE,
                RANGER_STATION,
                BICYCLE_SERVICE,
            )
        }
    }

    /**
     * `550-5520` Leisure: A park that contains rides and/or other entertainment which may be based
     * on a central theme.
     */
    object Leisure : PlaceCategorySubgroup("550-5520", "Leisure") {
        override val group: PlaceCategoryGroup get() = LeisureAndOutdoor

        /**
         * `550-5520-0000` Leisure: An establishment that provides rides or other entertainment.
         * This is a base-level category that should be used for all places that do not fit other
         * categories defined forAmusement or Holiday Park (550-5520-xxxx).
         */
        val LEISURE: PlaceCategoryLeaf = leaf("550-5520-0000", "Leisure")

        /**
         * `550-5520-0207` Amusement Park: An establishment that provides a park containing rides or
         * other entertainment. Certain amusement parks may be theme-based. For example, Disney
         * World, Sea World or Six Flags.
         */
        val AMUSEMENT_PARK: PlaceCategoryLeaf = leaf("550-5520-0207", "Amusement Park")

        /**
         * `550-5520-0208` Zoo: An establishment where animals are exhibited in cages or large
         * enclosures for the public.
         */
        val ZOO: PlaceCategoryLeaf = leaf("550-5520-0208", "Zoo")

        /**
         * `550-5520-0209` Wild Animal Park: An establishment where wild animals are exhibited in an
         * open environment for the public.
         */
        val WILD_ANIMAL_PARK: PlaceCategoryLeaf = leaf("550-5520-0209", "Wild Animal Park")

        /**
         * `550-5520-0210` Wildlife Refuge: An establishment where animals are exhibited in their
         * natural environment for the public.
         */
        val WILDLIFE_REFUGE: PlaceCategoryLeaf = leaf("550-5520-0210", "Wildlife Refuge")

        /**
         * `550-5520-0211` Aquarium: An establishment where fish and other aquatic life are
         * exhibited.
         */
        val AQUARIUM: PlaceCategoryLeaf = leaf("550-5520-0211", "Aquarium")

        /**
         * `550-5520-0212` Ski Resort: An establishment that offers downhill skiing, snowboarding
         * and other winter sports. Ski resorts generally include multiple ski lifts, lodging
         * facilities and other related amenities. Ski resorts are often nationally or regionally
         * recognized.
         */
        val SKI_RESORT: PlaceCategoryLeaf = leaf("550-5520-0212", "Ski Resort")

        /**
         * `550-5520-0228` Animal Park: An establishment where various species of animals are
         * exhibited for the public. Examples of animal parks include zoos, aquariums, wild animal
         * parks or wildlife refuges.
         */
        val ANIMAL_PARK: PlaceCategoryLeaf = leaf("550-5520-0228", "Animal Park")

        /**
         * `550-5520-0357` Water Park: An establishment that provides access to recreational water
         * areas. Water parks generally include water slides, splash pads, spray grounds (water
         * playgrounds), lazy rivers or other related amenities.
         */
        val WATER_PARK: PlaceCategoryLeaf = leaf("550-5520-0357", "Water Park")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                LEISURE,
                AMUSEMENT_PARK,
                ZOO,
                WILD_ANIMAL_PARK,
                WILDLIFE_REFUGE,
                AQUARIUM,
                SKI_RESORT,
                ANIMAL_PARK,
                WATER_PARK,
            )
        }
    }

    override val subgroups: List<PlaceCategorySubgroup> by lazy {
        listOf(
            OutdoorRecreation,
            Leisure,
        )
    }
}
