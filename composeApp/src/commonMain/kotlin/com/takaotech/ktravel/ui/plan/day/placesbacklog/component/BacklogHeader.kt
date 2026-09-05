package com.takaotech.ktravel.ui.plan.day.placesbacklog.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.ui.plan.day.placesbacklog.PlacesBacklogTestTags
import com.takaotech.ktravel.ui.theme.KTravelTheme
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.close
import ktravel.composeapp.generated.resources.planning_detail_cd_close_backlog
import ktravel.composeapp.generated.resources.planning_detail_places_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Title, how many places are still waiting, and the way out of the pane. */
@Composable
internal fun BacklogHeader(count: Int, onCloseClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(Res.string.planning_detail_places_title),
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        if (count > 0) {
            CountBadge(count)
        }

        IconButton(
            modifier = Modifier.testTag(PlacesBacklogTestTags.CLOSE_BUTTON),
            onClick = onCloseClick,
        ) {
            Icon(
                painter = painterResource(Res.drawable.close),
                contentDescription = stringResource(Res.string.planning_detail_cd_close_backlog),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BacklogHeaderPreview(@PreviewParameter(BacklogHeaderPreviewParams::class) count: Int) = KTravelTheme {
    Surface {
        BacklogHeader(
            count = count,
            onCloseClick = {},
            modifier = Modifier.padding(bottom = 8.dp),
        )
    }
}

/**
 * The counts that change how the header draws.
 *
 * Zero is the one that removes the badge altogether, and the three digit count is where the badge
 * stops being a circle and starts competing with the title for the row: both have to stay readable.
 */
internal class BacklogHeaderPreviewParams : PreviewParameterProvider<Int> {
    override val values = sequenceOf(0, 3, 128)
}
