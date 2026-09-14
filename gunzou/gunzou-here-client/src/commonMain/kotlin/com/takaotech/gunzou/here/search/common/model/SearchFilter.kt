package com.takaotech.gunzou.here.search.common.model

/**
 * An inclusion and exclusion list of ids, the shape HERE search filters such as `categories` and
 * `foodTypes` share.
 *
 * A result matches when it carries any included id and no excluded one: an exclusion always wins,
 * so restaurants that are not fast food are `include = [restaurant]`, `exclude = [fast food]`.
 *
 * @param T What the filter selects: a typed category, or a raw id where this client has no table
 * @property include Values a result must match at least one of. Empty means no inclusion constraint.
 * @property exclude Values a result must match none of
 */
data class SearchFilter<T>(val include: List<T>, val exclude: List<T> = emptyList()) {
    init {
        require(include.isNotEmpty() || exclude.isNotEmpty()) { "A filter needs at least one value" }
    }

    /**
     * Converts the filter to the comma-separated form HERE expects, with a `!` in front of every
     * excluded id.
     *
     * @param idOf Maps a value to the id HERE knows it by
     */
    fun toQueryString(idOf: (T) -> String): String = buildList {
        include.forEach { add(idOf(it)) }
        exclude.forEach { add("!${idOf(it)}") }
    }.joinToString(",")
}
