package com.takaotech.ktravel.gunzou.server.endpoint.here.search

import com.takaotech.gunzou.api.search.GeoBounds
import com.takaotech.gunzou.api.search.PlaceCategoryGroup
import com.takaotech.gunzou.api.search.SearchAddress
import com.takaotech.gunzou.api.search.SearchResultType
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
import com.takaotech.gunzou.here.search.common.dto.MapView
import com.takaotech.gunzou.here.search.common.dto.ResultCategory
import com.vanniktech.locale.Country
import com.takaotech.gunzou.here.search.common.dto.SearchAddress as HereSearchAddress

// What every HERE search answer has in common, whichever of its APIs produced it.

private const val POSTAL_CODE_POINT = "postalCodePoint"
private const val LOCALITY_TYPE_POSTAL_CODE = "postalCode"
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

/**
 * HERE's result type in the contract's vocabulary.
 *
 * The two spellings of a postal code — a point, or a locality of type postal code — become one. A
 * type the contract does not know is passed through as it is, since the contract admits unknown
 * values; a missing one, which HERE documents as always present, is read as a place.
 */
internal fun hereResultType(resultType: String?, localityType: String?): SearchResultType = when {
    resultType == POSTAL_CODE_POINT -> SearchResultType.POSTAL_CODE

    resultType == SearchResultType.LOCALITY.value && localityType == LOCALITY_TYPE_POSTAL_CODE ->
        SearchResultType.POSTAL_CODE

    else -> resultType?.let(::SearchResultType) ?: SearchResultType.PLACE
}

/** The group of the primary category, or of the first one when HERE marks none as primary. */
internal fun List<ResultCategory>.toCategoryGroup(): PlaceCategoryGroup? {
    val category = firstOrNull { it.primary == true } ?: firstOrNull() ?: return null

    return CATEGORY_GROUPS[category.id.substringBefore(CATEGORY_ID_SEPARATOR)]
}

/** The address, with the country named by its alpha-2 code as the contract promises. */
internal fun HereSearchAddress.toSearchAddress(): SearchAddress = SearchAddress(
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

/** The extent HERE suggests framing the result in. */
internal fun MapView.toGeoBounds(): GeoBounds = GeoBounds(west = west, south = south, east = east, north = north)
