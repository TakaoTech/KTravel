package com.takaotech.gunzou.here.search.common.category

/**
 * The HERE Places Category System as a whole: every [PlaceCategoryGroup], and the lookup that turns
 * an id found in a response back into its [PlaceCategory].
 */
object PlaceCategories {
    /** Every level 1 category, in the order HERE documents them. */
    val groups: List<PlaceCategoryGroup> by lazy {
        listOf(
            EatAndDrink,
            GoingOutEntertainment,
            SightsAndMuseums,
            NaturalAndGeographical,
            Transport,
            Accommodations,
            LeisureAndOutdoor,
            Shopping,
            BusinessAndServices,
            Facilities,
            AreasAndBuildings,
        )
    }

    /** Every level 3 category, the level search results are tagged with. */
    val leaves: List<PlaceCategoryLeaf> by lazy {
        groups.flatMap { group -> group.subgroups.flatMap { it.categories } }
    }

    private val byId: Map<String, PlaceCategory> by lazy {
        buildMap {
            groups.forEach { group ->
                put(group.id, group)
                group.subgroups.forEach { subgroup ->
                    put(subgroup.id, subgroup)
                    subgroup.categories.forEach { put(it.id, it) }
                }
            }
        }
    }

    /**
     * Resolves an id of any level — `100`, `100-1000` or `100-1000-0001` — to its category.
     *
     * Returns null for an id this client does not know: HERE adds categories over time, so a
     * response may carry one that is newer than this table.
     */
    fun fromId(id: String): PlaceCategory? = byId[id]
}
