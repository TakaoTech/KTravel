package com.takaotech.ktravel.ui.plan.place.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.domain.search.model.PlaceCandidate
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/** Space around the rows of a place list. */
private val LIST_PADDING = PaddingValues(start = 12.dp, end = 12.dp, bottom = 12.dp)

/**
 * The frame every list in the list area shares: a small uppercase title with its count, then either
 * the rows or the [empty] block.
 *
 * @param isLoading Draws a small spinner beside the title; the rows stay while it runs.
 * @param empty What stands in for the rows when [places] is empty.
 * @param row One place. Called once per entry of [places].
 */
@Composable
internal fun PlaceListSection(
    title: String,
    places: ImmutableList<PlaceCandidate>,
    isLoading: Boolean,
    empty: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    row: @Composable (PlaceCandidate) -> Unit,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = places.size.toString(),
                style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
            }
        }

        if (places.isNotEmpty()) {
            LazyColumn(
                contentPadding = LIST_PADDING,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(places, key = { it.id }) { row(it) }
            }
        } else if (!isLoading) {
            Column(modifier = Modifier.fillMaxWidth(), content = empty)
        }
    }
}

/**
 * The block an empty list shows: a symbol, a sentence, and an optional way out.
 *
 * @param action A button that resolves the emptiness, such as removing a filter.
 */
@Composable
internal fun PlaceListMessage(
    icon: DrawableResource,
    text: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(34.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (action != null) {
            Spacer(Modifier.height(12.dp))
            action()
        }
    }
}
