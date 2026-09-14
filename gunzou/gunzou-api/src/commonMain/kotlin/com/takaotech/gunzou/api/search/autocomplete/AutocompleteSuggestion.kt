package com.takaotech.gunzou.api.search.autocomplete

import com.takaotech.gunzou.api.common.GeoPoint
import com.takaotech.gunzou.api.search.GeoBounds
import com.takaotech.gunzou.api.search.PlaceCategoryGroup
import com.takaotech.gunzou.api.search.SearchAddress
import com.takaotech.gunzou.api.search.SearchHighlights
import com.takaotech.gunzou.api.search.SearchResultType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * One suggestion for what the user is typing: a [Place] they can pick, or a [Query] they can run.
 *
 * Discriminated on `type`. The two are different on purpose and a client has to handle them apart:
 * picking a place puts a pin on the map, picking a query starts another search.
 */
@Serializable
sealed interface AutocompleteSuggestion {

    /** Provider identifier of the suggestion, stable for as long as the provider keeps it. */
    val id: String

    /** Display name, in the language of the result. */
    val title: String

    /**
     * A place, an address or an area: something with a position.
     *
     * @property id Provider identifier of the result.
     * @property title Display name, in the language of the result.
     * @property resultType What it is: a place, a house number, a street, a locality...
     * @property position Where to put the pin.
     * @property address Postal address.
     * @property category The broad kind of place, when [resultType] is
     *   [SearchResultType.PLACE] and the provider categorizes it.
     * @property distanceMeters Straight-line distance from the request's `at`, in meters, when the
     *   request had one.
     * @property bounds The extent it covers, for results larger than a point. Places have none.
     * @property highlights Which parts of the text matched the query, when the provider reports it.
     */
    @Serializable
    @SerialName("place")
    data class Place(
        @SerialName("id") override val id: String,
        @SerialName("title") override val title: String,
        @SerialName("resultType") val resultType: SearchResultType,
        @SerialName("position") val position: GeoPoint,
        @SerialName("address") val address: SearchAddress = SearchAddress(),
        @SerialName("category") val category: PlaceCategoryGroup? = null,
        @SerialName("distanceMeters") val distanceMeters: Long? = null,
        @SerialName("bounds") val bounds: GeoBounds? = null,
        @SerialName("highlights") val highlights: SearchHighlights? = null,
    ) : AutocompleteSuggestion

    /**
     * A search to offer rather than a place, such as "Restaurants near Rome" or a chain of shops.
     *
     * It has no position: what it leads to is a list of places, found by a follow-up search. Only a
     * provider that [suggests queries][com.takaotech.gunzou.api.search.SearchProfileDescriptor.supportsQuerySuggestions]
     * produces it.
     *
     * The provider's own follow-up URL is deliberately not carried: it names the provider's host and
     * its parameters, which is what this contract exists to keep away from a client.
     *
     * @property id Provider identifier of the suggestion. It does not identify a place.
     * @property title Display name, in the language of the result.
     * @property kind Whether it searches a category or a chain.
     * @property searchText The text the follow-up search would run, when the provider says it. A
     *   client can put it back into the query field.
     * @property highlights Which parts of the title matched the query, when the provider reports it.
     */
    // TODO add a provider neutral follow-up search endpoint (browse by category or chain) and carry
    //  here what it needs to be called.
    @Serializable
    @SerialName("query")
    data class Query(
        @SerialName("id") override val id: String,
        @SerialName("title") override val title: String,
        @SerialName("kind") val kind: QuerySuggestionKind,
        @SerialName("searchText") val searchText: String? = null,
        @SerialName("highlights") val highlights: SearchHighlights? = null,
    ) : AutocompleteSuggestion
}

/**
 * What a [AutocompleteSuggestion.Query] searches for.
 *
 * A string rather than an enum, so a kind added by a newer server still decodes.
 *
 * @property value The kind as it travels on the wire.
 */
@Serializable
@JvmInline
value class QuerySuggestionKind(val value: String) {
    override fun toString(): String = value

    /** The kinds this contract knows. */
    companion object {
        /** Places of one category, such as restaurants. */
        val CATEGORY = QuerySuggestionKind("category")

        /** Places of one chain, such as a brand of supermarkets. */
        val CHAIN = QuerySuggestionKind("chain")
    }
}
