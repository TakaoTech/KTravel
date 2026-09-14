package com.takaotech.ktravel.gunzou.server.endpoint.here.search

import com.takaotech.gunzou.api.common.GeoPoint
import com.takaotech.gunzou.api.search.GeoBounds
import com.takaotech.gunzou.api.search.PlaceCategoryGroup
import com.takaotech.gunzou.api.search.SearchAddress
import com.takaotech.gunzou.api.search.SearchHighlights
import com.takaotech.gunzou.api.search.SearchResultType
import com.takaotech.gunzou.api.search.TextRange
import com.takaotech.gunzou.api.search.autocomplete.AutocompleteResponse
import com.takaotech.gunzou.api.search.autocomplete.AutocompleteSuggestion
import com.takaotech.gunzou.api.search.autocomplete.QuerySuggestionKind
import com.takaotech.gunzou.api.search.autocomplete.TermSuggestion
import com.takaotech.gunzou.here.search.autosuggest.dto.response.AutosuggestResponse
import com.takaotech.gunzou.here.search.autosuggest.dto.response.AutosuggestResultItem
import com.takaotech.gunzou.here.search.common.category.Accommodations
import com.takaotech.gunzou.here.search.common.category.AreasAndBuildings
import com.takaotech.gunzou.here.search.common.category.BusinessAndServices
import com.takaotech.gunzou.here.search.common.category.EatAndDrink
import com.takaotech.gunzou.here.search.common.category.Facilities
import com.takaotech.gunzou.here.search.common.category.GoingOutEntertainment
import com.takaotech.gunzou.here.search.common.category.LeisureAndOutdoor
import com.takaotech.gunzou.here.search.common.category.NaturalAndGeographical
import com.takaotech.gunzou.here.search.common.category.Shopping
import com.takaotech.gunzou.here.search.common.category.SightsAndMuseums
import com.takaotech.gunzou.here.search.common.category.Transport
import com.takaotech.gunzou.here.search.common.dto.ResultCategory
import com.takaotech.gunzou.here.search.common.dto.highlight.HighlightRange
import com.takaotech.gunzou.here.search.common.dto.highlight.Highlights
import com.vanniktech.locale.Country
import io.ktor.http.parseUrl
import com.takaotech.gunzou.here.search.common.dto.SearchAddress as HereSearchAddress

// What this server makes of a HERE Autosuggest answer.

private const val CATEGORY_QUERY = "categoryQuery"
private const val CHAIN_QUERY = "chainQuery"
private const val QUERY_SUFFIX = "Query"
private const val POSTAL_CODE_POINT = "postalCodePoint"
private const val LOCALITY_TYPE_POSTAL_CODE = "postalCode"
private const val FOLLOW_UP_QUERY_PARAMETER = "q"
private const val CATEGORY_ID_SEPARATOR = '-'

/**
 * The level 1 groups of the HERE Places Category System, by the id every category below them starts
 * with.
 *
 * Keyed on the id prefix rather than resolved through the vendor's category table, so a category HERE
 * added after that table was written still lands in its group.
 */
private val CATEGORY_GROUPS: Map<String, PlaceCategoryGroup> = mapOf(
    EatAndDrink.id to PlaceCategoryGroup.EAT_AND_DRINK,
    GoingOutEntertainment.id to PlaceCategoryGroup.GOING_OUT_ENTERTAINMENT,
    SightsAndMuseums.id to PlaceCategoryGroup.SIGHTS_AND_MUSEUMS,
    NaturalAndGeographical.id to PlaceCategoryGroup.NATURAL_AND_GEOGRAPHICAL,
    Transport.id to PlaceCategoryGroup.TRANSPORT,
    Accommodations.id to PlaceCategoryGroup.ACCOMMODATIONS,
    LeisureAndOutdoor.id to PlaceCategoryGroup.LEISURE_AND_OUTDOOR,
    Shopping.id to PlaceCategoryGroup.SHOPPING,
    BusinessAndServices.id to PlaceCategoryGroup.BUSINESS_AND_SERVICES,
    Facilities.id to PlaceCategoryGroup.FACILITIES,
    AreasAndBuildings.id to PlaceCategoryGroup.AREAS_AND_BUILDINGS,
)

/** Translates a HERE Autosuggest answer into the provider neutral autocomplete answer. */
internal fun AutosuggestResponse.toAutocompleteResponse(): AutocompleteResponse = AutocompleteResponse(
    suggestions = items.mapNotNull { it.toSuggestion() },
    termSuggestions = queryTerms.map { term ->
        TermSuggestion(term = term.term, replaces = term.replaces, start = term.start, end = term.end)
    },
)

private fun AutosuggestResultItem.toSuggestion(): AutocompleteSuggestion? = when (this) {
    is AutosuggestResultItem.Entity -> toPlace()
    is AutosuggestResultItem.Query -> toQuery()
}

/**
 * A place, or nothing when HERE sent one without a position.
 *
 * The contract promises a pin on every place, and a suggestion that cannot be put on the map is not
 * one worth offering: dropping it is better than a position a client would have to special case.
 */
private fun AutosuggestResultItem.Entity.toPlace(): AutocompleteSuggestion.Place? {
    val position = position ?: return null

    return AutocompleteSuggestion.Place(
        id = id,
        title = title,
        resultType = toSearchResultType(),
        position = GeoPoint(lat = position.lat, lng = position.lng),
        address = address.toSearchAddress(),
        category = categories?.primaryOrFirst()?.toGroup(),
        distanceMeters = distance,
        bounds = mapView?.let { GeoBounds(west = it.west, south = it.south, east = it.east, north = it.north) },
        highlights = highlights?.toSearchHighlights(),
    )
}

/**
 * HERE's result type in the contract's vocabulary.
 *
 * The two spellings of a postal code — a point, or a locality of type postal code — become one. A
 * type the contract does not know is passed through as it is, since the contract admits unknown
 * values; a missing one, which HERE documents as always present, is read as a place.
 */
private fun AutosuggestResultItem.Entity.toSearchResultType(): SearchResultType = when {
    resultType == POSTAL_CODE_POINT -> SearchResultType.POSTAL_CODE

    resultType == SearchResultType.LOCALITY.value && localityType == LOCALITY_TYPE_POSTAL_CODE ->
        SearchResultType.POSTAL_CODE

    else -> resultType?.let(::SearchResultType) ?: SearchResultType.PLACE
}

private fun List<ResultCategory>.primaryOrFirst(): ResultCategory? = firstOrNull { it.primary == true } ?: firstOrNull()

private fun ResultCategory.toGroup(): PlaceCategoryGroup? = CATEGORY_GROUPS[id.substringBefore(CATEGORY_ID_SEPARATOR)]

/** The address, with the country named by its alpha-2 code as the contract promises. */
private fun HereSearchAddress.toSearchAddress(): SearchAddress = SearchAddress(
    label = label,
    countryCode = countryCode?.let { Country.fromOrNull(it)?.code },
    countryName = countryName,
    state = state,
    county = county,
    city = city,
    district = district,
    street = street,
    postalCode = postalCode,
    houseNumber = houseNumber,
)

private fun Highlights.toSearchHighlights(): SearchHighlights = SearchHighlights(
    title = title.orEmpty().map { it.toTextRange() },
    addressLabel = address?.label.orEmpty().map { it.toTextRange() },
)

private fun HighlightRange.toTextRange(): TextRange = TextRange(start = start, end = end)

/**
 * A category or chain search, or nothing when HERE sent a kind of query it does not name.
 *
 * The follow-up URL is read for its search text and then dropped: it names HERE's host and
 * parameters, which the contract keeps away from a client.
 */
private fun AutosuggestResultItem.Query.toQuery(): AutocompleteSuggestion.Query? {
    val kind = when (resultType) {
        CATEGORY_QUERY -> QuerySuggestionKind.CATEGORY

        CHAIN_QUERY -> QuerySuggestionKind.CHAIN

        null -> return null

        // A kind added after this was written, passed through in the same spelling as the others.
        else -> QuerySuggestionKind(resultType.orEmpty().removeSuffix(QUERY_SUFFIX))
    }

    return AutocompleteSuggestion.Query(
        id = id,
        title = title,
        kind = kind,
        searchText = href?.let(::parseUrl)?.parameters?.get(FOLLOW_UP_QUERY_PARAMETER),
        highlights = highlights?.toSearchHighlights(),
    )
}
