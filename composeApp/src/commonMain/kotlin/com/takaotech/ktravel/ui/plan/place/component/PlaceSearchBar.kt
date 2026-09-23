package com.takaotech.ktravel.ui.plan.place.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.domain.routing.ProfileAvailability
import com.takaotech.ktravel.domain.search.PlaceSearchProvider
import com.takaotech.ktravel.domain.search.PlaceSearchProviderOption
import com.takaotech.ktravel.domain.search.model.PlaceCandidate
import com.takaotech.ktravel.domain.search.model.PlaceCandidateSource
import com.takaotech.ktravel.ui.plan.place.previewCoordinates
import com.takaotech.ktravel.ui.plan.place.previewHere
import com.takaotech.ktravel.ui.plan.place.previewLongAddress
import com.takaotech.ktravel.ui.plan.place.previewProviders
import com.takaotech.ktravel.ui.plan.place.previewTorrazzo
import com.takaotech.ktravel.ui.theme.KTravelTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.arrow_drop_down
import ktravel.composeapp.generated.resources.check
import ktravel.composeapp.generated.resources.close
import ktravel.composeapp.generated.resources.place_insert_cd_clear_search
import ktravel.composeapp.generated.resources.place_insert_cd_provider
import ktravel.composeapp.generated.resources.place_insert_provider_header
import ktravel.composeapp.generated.resources.place_insert_provider_missing_api_key
import ktravel.composeapp.generated.resources.place_insert_provider_not_served
import ktravel.composeapp.generated.resources.place_insert_provider_unreachable
import ktravel.composeapp.generated.resources.place_insert_results_from
import ktravel.composeapp.generated.resources.place_insert_search_placeholder
import ktravel.composeapp.generated.resources.place_insert_use_coordinates
import ktravel.composeapp.generated.resources.search
import ktravel.composeapp.generated.resources.travel_explore
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Height of the search bar, and of the filter button beside it.
 *
 * It is what Material gives its search input field, repeated here so the button lines up with a bar
 * that measures itself.
 */
internal val SEARCH_BAR_HEIGHT = 56.dp

/** Elevation of everything floating over the map. */
internal val FLOATING_ELEVATION = 6.dp

/** Rotation of the provider arrow while its menu is open. */
private const val OPEN_ARROW_DEGREES = 180f

/**
 * The search bar floating over the map, with the provider picker at its end and the suggestions
 * under it.
 *
 * It is a Material [DockedSearchBar]: the suggestions open inside the bar itself rather than in a
 * popup, so they never take the focus away from the text being typed, and the bar keeps its own
 * shape, colours and search semantics.
 *
 * The bar is told it is expanded as soon as the traveller is typing in it, while the suggestions
 * only open once there is something to show. Were the two the same, an answer that came back empty
 * would drop the keyboard mid word.
 *
 * @param suggestions The first search results, closest first.
 * @param coordinateCandidate The place [query] spells out when it is a `lat, lng` pair, offered as the
 *   first row.
 * @param isSuggestionsExpanded Whether the bar is open; hoisted so tapping the map can close it.
 * @param onClear Empties the bar.
 * @param onSuggestionChosen A row of the suggestions was picked; the bar closes itself.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PlaceSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    suggestions: ImmutableList<PlaceCandidate>,
    coordinateCandidate: PlaceCandidate?,
    providers: ImmutableList<PlaceSearchProviderOption>,
    selectedProvider: PlaceSearchProvider?,
    isSuggestionsExpanded: Boolean,
    onClear: () -> Unit,
    onSuggestionChosen: (PlaceCandidate) -> Unit,
    onProviderSelect: (PlaceSearchProvider) -> Unit,
    onSuggestionsExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val hasSuggestions = coordinateCandidate != null || suggestions.isNotEmpty()

    DockedSearchBar(
        inputField = {
            SearchBarDefaults.InputField(
                modifier = Modifier.fillMaxWidth(),
                query = query,
                onQueryChange = {
                    onQueryChange(it)
                    onSuggestionsExpandedChange(it.isNotBlank())
                },
                onSearch = {
                    onSuggestionsExpandedChange(false)
                    focusManager.clearFocus()
                },
                expanded = isSuggestionsExpanded,
                onExpandedChange = onSuggestionsExpandedChange,
                placeholder = { Text(stringResource(Res.string.place_insert_search_placeholder)) },
                leadingIcon = {
                    Icon(painter = painterResource(Res.drawable.search), contentDescription = null)
                },
                trailingIcon = {
                    SearchBarActions(
                        query = query,
                        providers = providers,
                        selectedProvider = selectedProvider,
                        onClear = {
                            onClear()
                            onSuggestionsExpandedChange(false)
                        },
                        onProviderSelect = onProviderSelect,
                    )
                },
            )
        },
        expanded = isSuggestionsExpanded && hasSuggestions,
        onExpandedChange = onSuggestionsExpandedChange,
        modifier = modifier,
        shadowElevation = FLOATING_ELEVATION,
    ) {
        SuggestionsList(
            suggestions = suggestions,
            coordinateCandidate = coordinateCandidate,
            providerName = selectedProvider?.name,
            onSuggestionChosen = {
                onSuggestionsExpandedChange(false)
                focusManager.clearFocus()
                onSuggestionChosen(it)
            },
        )
    }
}

/**
 * What sits at the end of the bar: the button that empties it, and the provider it searches with.
 *
 * The provider steps aside while something is being looked up, where the name of the service matters
 * less than the room the words need.
 */
@Composable
private fun SearchBarActions(
    query: String,
    providers: ImmutableList<PlaceSearchProviderOption>,
    selectedProvider: PlaceSearchProvider?,
    onClear: () -> Unit,
    onProviderSelect: (PlaceSearchProvider) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (query.isNotEmpty()) {
            IconButton(onClick = onClear) {
                Icon(
                    painter = painterResource(Res.drawable.close),
                    contentDescription = stringResource(Res.string.place_insert_cd_clear_search),
                )
            }
        }
        if (selectedProvider != null) {
            AnimatedVisibility(query.isEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    VerticalDivider(modifier = Modifier.height(26.dp))
                    ProviderPicker(
                        providers = providers,
                        selectedProvider = selectedProvider,
                        onProviderSelect = onProviderSelect,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProviderPicker(
    providers: ImmutableList<PlaceSearchProviderOption>,
    selectedProvider: PlaceSearchProvider,
    onProviderSelect: (PlaceSearchProvider) -> Unit,
) {
    var isExpanded by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    Box {
        Surface(
            onClick = { isExpanded = true },
            modifier = Modifier.height(38.dp),
            shape = CircleShape,
            color = if (isExpanded) colors.secondaryContainer else colors.surfaceContainerHighest,
            contentColor = if (isExpanded) colors.onSecondaryContainer else colors.onSurfaceVariant,
        ) {
            Row(
                modifier = Modifier.padding(start = 12.dp, end = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = selectedProvider.name, style = MaterialTheme.typography.labelLarge)
                Icon(
                    painter = painterResource(Res.drawable.arrow_drop_down),
                    contentDescription = stringResource(
                        Res.string.place_insert_cd_provider,
                        selectedProvider.name,
                    ),
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(if (isExpanded) OPEN_ARROW_DEGREES else 0f),
                )
            }
        }

        DropdownMenu(expanded = isExpanded, onDismissRequest = { isExpanded = false }) {
            MenuHeader(stringResource(Res.string.place_insert_provider_header))
            providers.forEach { option ->
                ProviderMenuItem(
                    option = option,
                    isSelected = option.provider.id == selectedProvider.id,
                    onClick = {
                        isExpanded = false
                        onProviderSelect(option.provider)
                    },
                )
            }
        }
    }
}

@Composable
private fun ProviderMenuItem(option: PlaceSearchProviderOption, isSelected: Boolean, onClick: () -> Unit) {
    DropdownMenuItem(
        text = {
            Column {
                Text(option.provider.name)
                option.unavailableReason?.let {
                    Text(
                        text = stringResource(it),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        onClick = onClick,
        enabled = option.isSelectable,
        leadingIcon = {
            Icon(
                painter = painterResource(Res.drawable.travel_explore),
                contentDescription = null,
            )
        },
        trailingIcon = if (isSelected) {
            { Icon(painterResource(Res.drawable.check), contentDescription = null) }
        } else {
            null
        },
    )
}

/** The small uppercase title opening a menu of the search bar. */
@Composable
internal fun MenuHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

/** Why a provider cannot be picked, or `null` when it can. */
private val PlaceSearchProviderOption.unavailableReason: StringResource?
    get() = when (availability) {
        ProfileAvailability.Available -> null
        ProfileAvailability.MissingApiKey -> Res.string.place_insert_provider_missing_api_key
        ProfileAvailability.NotServed -> Res.string.place_insert_provider_not_served
        is ProfileAvailability.NavigatorUnreachable -> Res.string.place_insert_provider_unreachable
    }

/**
 * What the bar opens onto: the coordinate row first when the query is a pair of degrees, then the
 * closest results, and last the service they came from.
 *
 * It scrolls on its own, since the bar bounds how far it may grow.
 */
@Composable
private fun SuggestionsList(
    suggestions: ImmutableList<PlaceCandidate>,
    coordinateCandidate: PlaceCandidate?,
    providerName: String?,
    onSuggestionChosen: (PlaceCandidate) -> Unit,
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(6.dp),
    ) {
        coordinateCandidate?.let { candidate ->
            SuggestionRow(candidate = candidate, onClick = { onSuggestionChosen(candidate) })
        }
        suggestions.forEach { candidate ->
            SuggestionRow(candidate = candidate, onClick = { onSuggestionChosen(candidate) })
        }

        if (suggestions.isNotEmpty() && providerName != null) {
            HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
            Row(
                modifier = Modifier.padding(start = 10.dp, end = 10.dp, top = 8.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    painter = painterResource(Res.drawable.travel_explore),
                    contentDescription = null,
                    modifier = Modifier.size(15.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(Res.string.place_insert_results_from, providerName),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SuggestionRow(candidate: PlaceCandidate, onClick: () -> Unit) {
    val isCoordinate = candidate.source == PlaceCandidateSource.COORDINATES
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            painter = painterResource(candidate.icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Column(modifier = Modifier.weight(1f)) {
            val titleStyle = MaterialTheme.typography.bodyMedium
            Text(
                text = candidate.title,
                style = if (isCoordinate) titleStyle.copy(fontFamily = FontFamily.Monospace) else titleStyle,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = if (isCoordinate) {
                    stringResource(Res.string.place_insert_use_coordinates)
                } else {
                    listOfNotNull(
                        candidate.category?.let { stringResource(it.label) },
                        candidate.locality,
                    ).joinToString(" · ")
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        candidate.distanceMeters?.let {
            Text(
                text = distanceLabel(it),
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

//region Previews
@PreviewScreenSizes
@Composable
private fun PlaceSearchBarPreview() = KTravelTheme {
    Surface {
        var query by remember {
            mutableStateOf("Torrazzo")
        }

        PlaceSearchBar(
            query = query,
            suggestions = persistentListOf(previewTorrazzo, previewLongAddress),
            coordinateCandidate = null,
            providers = previewProviders,
            selectedProvider = previewHere,
            isSuggestionsExpanded = true,
            onQueryChange = {
                query = it
            },
            onClear = {
                query = ""
            },
            onSuggestionChosen = {},
            onProviderSelect = {},
            onSuggestionsExpandedChange = {},
            modifier = Modifier
                .padding(12.dp)
                .width(420.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PlaceSearchBarCoordinatesPreview() = KTravelTheme {
    Surface {
        PlaceSearchBar(
            query = "45.26, 9.34",
            suggestions = persistentListOf(),
            coordinateCandidate = previewCoordinates,
            providers = previewProviders,
            selectedProvider = previewHere,
            isSuggestionsExpanded = true,
            onQueryChange = {},
            onClear = {},
            onSuggestionChosen = {},
            onProviderSelect = {},
            onSuggestionsExpandedChange = {},
            modifier = Modifier
                .padding(12.dp)
                .width(360.dp),
        )
    }
}
//endregion Previews
