package com.takaotech.ktravel.ui.plan.day.placesbacklog.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.ui.plan.day.placesbacklog.PlacesBacklogTestTags
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.planning_detail_places_empty
import ktravel.composeapp.generated.resources.planning_detail_places_empty_hint
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun BacklogEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            modifier = Modifier.testTag(PlacesBacklogTestTags.EMPTY),
            text = stringResource(Res.string.planning_detail_places_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Text(
            modifier = Modifier.testTag(PlacesBacklogTestTags.EMPTY_HINT),
            text = stringResource(Res.string.planning_detail_places_empty_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
