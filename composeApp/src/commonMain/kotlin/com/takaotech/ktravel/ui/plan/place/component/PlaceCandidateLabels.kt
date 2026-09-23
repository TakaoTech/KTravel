package com.takaotech.ktravel.ui.plan.place.component

import androidx.compose.runtime.Composable
import com.takaotech.ktravel.domain.search.model.PlaceCandidate
import com.takaotech.ktravel.domain.search.model.PlaceCandidateSource
import com.takaotech.ktravel.domain.search.model.PlaceCategory
import com.takaotech.ktravel.presentation.place.PlaceSearchProblem
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.hotel
import ktravel.composeapp.generated.resources.museum
import ktravel.composeapp.generated.resources.my_location
import ktravel.composeapp.generated.resources.park
import ktravel.composeapp.generated.resources.place
import ktravel.composeapp.generated.resources.place_insert_category_accommodation
import ktravel.composeapp.generated.resources.place_insert_category_eat_and_drink
import ktravel.composeapp.generated.resources.place_insert_category_nature
import ktravel.composeapp.generated.resources.place_insert_category_sights_and_museums
import ktravel.composeapp.generated.resources.place_insert_coordinates
import ktravel.composeapp.generated.resources.place_insert_distance_kilometers
import ktravel.composeapp.generated.resources.place_insert_distance_meters
import ktravel.composeapp.generated.resources.place_insert_problem_missing_api_key
import ktravel.composeapp.generated.resources.place_insert_problem_navigator_unreachable
import ktravel.composeapp.generated.resources.place_insert_problem_not_authenticated
import ktravel.composeapp.generated.resources.place_insert_problem_provider_unavailable
import ktravel.composeapp.generated.resources.place_insert_problem_rate_limited
import ktravel.composeapp.generated.resources.place_insert_problem_unexpected
import ktravel.composeapp.generated.resources.restaurant
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Below this distance a place is measured in metres, above it in kilometres. */
private const val METERS_PER_KILOMETER = 1_000L

/** Metres in a tenth of a kilometre, the precision a distance in kilometres is shown with. */
private const val METERS_PER_TENTH = 100L

/** Tenths in a kilometre. */
private const val TENTHS_PER_KILOMETER = 10L

/** How far a place is, one decimal of kilometre past a kilometre. */
@Composable
internal fun distanceLabel(meters: Long): String = if (meters < METERS_PER_KILOMETER) {
    stringResource(Res.string.place_insert_distance_meters, meters.toInt())
} else {
    val tenths = (meters + METERS_PER_TENTH / 2) / METERS_PER_TENTH
    stringResource(
        Res.string.place_insert_distance_kilometers,
        (tenths / TENTHS_PER_KILOMETER).toInt(),
        (tenths % TENTHS_PER_KILOMETER).toInt(),
    )
}

/** The localized name of a category. */
internal val PlaceCategory.label: StringResource
    get() = when (this) {
        PlaceCategory.SIGHTS_AND_MUSEUMS -> Res.string.place_insert_category_sights_and_museums
        PlaceCategory.NATURE -> Res.string.place_insert_category_nature
        PlaceCategory.EAT_AND_DRINK -> Res.string.place_insert_category_eat_and_drink
        PlaceCategory.ACCOMMODATION -> Res.string.place_insert_category_accommodation
    }

/** The symbol a category is drawn with. */
internal val PlaceCategory.icon: DrawableResource
    get() = when (this) {
        PlaceCategory.SIGHTS_AND_MUSEUMS -> Res.drawable.museum
        PlaceCategory.NATURE -> Res.drawable.park
        PlaceCategory.EAT_AND_DRINK -> Res.drawable.restaurant
        PlaceCategory.ACCOMMODATION -> Res.drawable.hotel
    }

/** The symbol a candidate is drawn with: its category, a crosshair for coordinates, a pin otherwise. */
internal val PlaceCandidate.icon: DrawableResource
    get() = category?.icon ?: when (source) {
        PlaceCandidateSource.COORDINATES -> Res.drawable.my_location
        PlaceCandidateSource.SEARCH, PlaceCandidateSource.NEARBY -> Res.drawable.place
    }

/**
 * The line under a candidate's name: its category, its locality and its distance, whichever are
 * known, or the word for coordinates when it is nothing but a coordinate.
 */
@Composable
internal fun PlaceCandidate.supportingText(): String {
    val parts = buildList {
        if (source == PlaceCandidateSource.COORDINATES) add(stringResource(Res.string.place_insert_coordinates))
        category?.let { add(stringResource(it.label)) }
        locality?.let { add(it) }
        distanceMeters?.let { add(distanceLabel(it)) }
    }
    return parts.joinToString(separator = " · ")
}

/** The sentence telling the traveller why a list is empty, and what to do about it. */
internal val PlaceSearchProblem.message: StringResource
    get() = when (this) {
        PlaceSearchProblem.MISSING_API_KEY -> Res.string.place_insert_problem_missing_api_key
        PlaceSearchProblem.NAVIGATOR_UNREACHABLE -> Res.string.place_insert_problem_navigator_unreachable
        PlaceSearchProblem.NOT_AUTHENTICATED -> Res.string.place_insert_problem_not_authenticated
        PlaceSearchProblem.RATE_LIMITED -> Res.string.place_insert_problem_rate_limited
        PlaceSearchProblem.PROVIDER_UNAVAILABLE -> Res.string.place_insert_problem_provider_unavailable
        PlaceSearchProblem.UNEXPECTED -> Res.string.place_insert_problem_unexpected
    }
