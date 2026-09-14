package com.takaotech.gunzou.here.search.common.category

/**
 * A node of the HERE Places Category System, shared by every HERE search endpoint.
 *
 * The system has three levels, and a category filter accepts an id from any of them: a
 * [PlaceCategoryGroup] such as `100` (Eat and Drink), a [PlaceCategorySubgroup] such as `100-1000`
 * (Restaurant) and a [PlaceCategoryLeaf] such as `100-1000-0001` (Casual Dining). Results only
 * carry leaf ids. The whole tree is declared as nested objects, reachable as
 * `EatAndDrink.Restaurant.CASUAL_DINING`, and [PlaceCategories] resolves an id back to its node.
 *
 * The tree mirrors the
 * [HERE Places Category System](https://docs.here.com/geocoding-and-search/docs/places-category-system-full).
 *
 * @property id Identifier HERE uses in requests and responses
 * @property name English name of the category in the HERE documentation. Responses carry their own,
 *   localized in the requested language.
 */
sealed interface PlaceCategory {
    val id: String
    val name: String
}

/**
 * A level 1 category: a high-level grouping such as Eat and Drink or Transport.
 *
 * @property subgroups The level 2 categories of this group, in the order HERE documents them
 */
abstract class PlaceCategoryGroup internal constructor(
    final override val id: String,
    final override val name: String,
) : PlaceCategory {
    abstract val subgroups: List<PlaceCategorySubgroup>

    override fun toString(): String = "$id $name"
}

/**
 * A level 2 category: a domain inside a [PlaceCategoryGroup], such as Restaurant.
 *
 * @property group The level 1 category this one belongs to
 * @property categories The level 3 categories of this subgroup, in the order HERE documents them
 */
abstract class PlaceCategorySubgroup internal constructor(
    final override val id: String,
    final override val name: String,
) : PlaceCategory {
    abstract val group: PlaceCategoryGroup
    abstract val categories: List<PlaceCategoryLeaf>

    /** Declares a level 3 category of this subgroup. */
    protected fun leaf(id: String, name: String): PlaceCategoryLeaf =
        PlaceCategoryLeaf(id = id, name = name, subgroup = this)

    override fun toString(): String = "$id $name"
}

/**
 * A level 3 category, the finest one and the only level a search result is tagged with.
 *
 * @property subgroup The level 2 category this one belongs to
 */
class PlaceCategoryLeaf internal constructor(
    override val id: String,
    override val name: String,
    val subgroup: PlaceCategorySubgroup,
) : PlaceCategory {
    override fun toString(): String = "$id $name"
}
