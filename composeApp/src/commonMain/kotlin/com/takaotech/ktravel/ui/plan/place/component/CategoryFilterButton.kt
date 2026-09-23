package com.takaotech.ktravel.ui.plan.place.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.domain.search.model.PlaceCategory
import com.takaotech.ktravel.ui.theme.KTravelTheme
import kotlinx.collections.immutable.toPersistentList
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.check
import ktravel.composeapp.generated.resources.place
import ktravel.composeapp.generated.resources.place_insert_category_all
import ktravel.composeapp.generated.resources.place_insert_cd_filter
import ktravel.composeapp.generated.resources.place_insert_filter_header
import ktravel.composeapp.generated.resources.tune
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * The round button beside the search bar that narrows the framed area to a kind of place.
 *
 * Drawn in the primary colour while a filter is in force, so an emptier list than expected is
 * explained at a glance.
 *
 * @param onCategorySelect Called with the category picked, or `null` for every kind of place.
 */
@Composable
internal fun CategoryFilterButton(
    category: PlaceCategory?,
    onCategorySelect: (PlaceCategory?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isExpanded by remember { mutableStateOf(false) }
    val isActive = category != null || isExpanded
    val colors = MaterialTheme.colorScheme

    val items = remember {
        PlaceCategory.entries.toPersistentList<PlaceCategory?>().addingAt(0, null)
    }

    Box(modifier = modifier) {
        Surface(
            onClick = { isExpanded = true },
            modifier = Modifier.size(SEARCH_BAR_HEIGHT),
            shape = CircleShape,
            color = if (isActive) colors.primary else colors.surface,
            contentColor = if (isActive) colors.onPrimary else colors.onSurfaceVariant,
            shadowElevation = FLOATING_ELEVATION,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(Res.drawable.tune),
                    contentDescription = stringResource(Res.string.place_insert_cd_filter),
                )
            }
        }

        DropdownMenu(expanded = isExpanded, onDismissRequest = { isExpanded = false }) {
            MenuHeader(stringResource(Res.string.place_insert_filter_header))
            items.forEach { entry ->
                DropdownMenuItem(
                    text = { Text(stringResource(entry?.label ?: Res.string.place_insert_category_all)) },
                    onClick = {
                        isExpanded = false
                        onCategorySelect(entry)
                    },
                    leadingIcon = {
                        Icon(painterResource(entry?.icon ?: Res.drawable.place), contentDescription = null)
                    },
                    trailingIcon = if (entry == category) {
                        { Icon(painterResource(Res.drawable.check), contentDescription = null) }
                    } else {
                        null
                    },
                )
            }
        }
    }
}

//region Previews
@Preview(showSystemUi = true)
@Composable
private fun CategoryFilterButtonPreview() = KTravelTheme {
    Surface {
        Row(modifier = Modifier.padding(12.dp)) {
            CategoryFilterButton(category = null, onCategorySelect = {})
            CategoryFilterButton(
                category = PlaceCategory.NATURE,
                onCategorySelect = {},
                modifier = Modifier.padding(start = 12.dp),
            )
        }
    }
}
//endregion Previews
