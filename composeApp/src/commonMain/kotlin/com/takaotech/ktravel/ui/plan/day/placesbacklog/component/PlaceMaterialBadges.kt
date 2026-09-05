package com.takaotech.ktravel.ui.plan.day.placesbacklog.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.presentation.plan.PlaceUi
import com.takaotech.ktravel.ui.plan.day.placesbacklog.PlacesBacklogTestTags
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.attach_file
import ktravel.composeapp.generated.resources.description
import ktravel.composeapp.generated.resources.planning_detail_cd_place_attachments
import ktravel.composeapp.generated.resources.planning_detail_cd_place_has_note
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * What the traveller has already attached to a place waiting in the backlog.
 *
 * Notes and files follow the place out of the itinerary, so without a mark here they would look
 * lost: the backlog row is the only place they are visible from while the place waits.
 */
@Composable
internal fun PlaceMaterialBadges(place: PlaceUi, modifier: Modifier = Modifier) {
    if (!place.hasNote && place.attachmentCount == 0) return

    Row(
        modifier = modifier.padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (place.hasNote) {
            Icon(
                modifier = Modifier
                    .size(16.dp)
                    .testTag(PlacesBacklogTestTags.noteBadgeTag(place.id)),
                painter = painterResource(Res.drawable.description),
                contentDescription = stringResource(Res.string.planning_detail_cd_place_has_note),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (place.attachmentCount > 0) {
            Row(
                modifier = Modifier.testTag(PlacesBacklogTestTags.attachmentBadgeTag(place.id)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Icon(
                    modifier = Modifier.size(16.dp),
                    painter = painterResource(Res.drawable.attach_file),
                    contentDescription = pluralStringResource(
                        Res.plurals.planning_detail_cd_place_attachments,
                        place.attachmentCount,
                        place.attachmentCount,
                    ),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = place.attachmentCount.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
