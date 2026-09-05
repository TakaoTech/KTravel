package com.takaotech.ktravel.ui.plan.day.placesbacklog.component

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.presentation.plan.PlaceUi
import com.takaotech.ktravel.ui.plan.day.placesbacklog.DeleteMode
import com.takaotech.ktravel.ui.plan.day.placesbacklog.PlacesBacklogTestTags
import com.takaotech.ktravel.ui.theme.KTravelTheme
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.add
import ktravel.composeapp.generated.resources.delete
import ktravel.composeapp.generated.resources.planning_detail_cd_delete_step
import ktravel.composeapp.generated.resources.planning_detail_cd_move_place_to_steps
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * A place waiting to enter the itinerary: the drag affordance of the design, the name, and the two
 * actions the backlog has always had — remove (with the permanent variant behind the menu) and
 * promote to a step of the day.
 */
@Composable
internal fun BacklogPlaceRow(
    place: PlaceUi,
    onDeleteClick: () -> Unit,
    onPermanentDeleteClick: () -> Unit,
    onMoveToStepsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier.padding(start = 10.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
//            //Add support D&D?
//            Icon(
//                modifier = Modifier.size(18.dp),
//                painter = painterResource(Res.drawable.drag_indicator),
//                contentDescription = stringResource(Res.string.planning_detail_cd_reorder_step),
//                tint = MaterialTheme.colorScheme.onSurfaceVariant,
//            )

            Text(
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        top = 10.dp,
                        bottom = 10.dp,
                    )
                    .basicMarquee(),
                text = place.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
            )

            PlaceMaterialBadges(place = place)

            var menuExpanded by remember { mutableStateOf(false) }
            IconButton(onClick = { menuExpanded = true }) {
                Icon(
                    painter = painterResource(Res.drawable.delete),
                    contentDescription = stringResource(Res.string.planning_detail_cd_delete_step),
                )
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
            ) {
                DeleteMode.entries.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(option.text),
                                color = if (option == DeleteMode.PERMANENT) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    Color.Unspecified
                                },
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            when (option) {
                                DeleteMode.GENERAL -> onDeleteClick()
                                DeleteMode.PERMANENT -> onPermanentDeleteClick()
                            }
                        },
                    )
                }
            }

            IconButton(
                modifier = Modifier.testTag(PlacesBacklogTestTags.moveToStepsTag(place.id)),
                onClick = onMoveToStepsClick,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.add),
                    contentDescription = stringResource(
                        Res.string.planning_detail_cd_move_place_to_steps,
                    ),
                )
            }
        }
    }
}

//region Previews

@Preview(showBackground = true)
@Composable
private fun BacklogPlaceRowPreview(@PreviewParameter(BacklogPlaceRowPreviewParams::class) place: PlaceUi) =
    KTravelTheme {
        Surface {
            BacklogPlaceRow(
                place = place,
                onDeleteClick = {},
                onPermanentDeleteClick = {},
                onMoveToStepsClick = {},
                modifier = Modifier.padding(12.dp),
            )
        }
    }

/**
 * The places whose material changes what the row has to fit next to the name.
 *
 * The long name comes with both badges and a two digit count on purpose: the name is the only part
 * that gives way, and it gives way by scrolling rather than by ellipsis, so this is where the room
 * left to the badges and the two buttons has to stay right. A static preview parks the marquee at
 * its start.
 */
internal class BacklogPlaceRowPreviewParams : PreviewParameterProvider<PlaceUi> {
    override val values = sequenceOf(
        PlaceUi(name = loremIpsumName(words = 2), lat = 0.0, lng = 0.0),
        PlaceUi(name = loremIpsumName(words = 3), lat = 0.0, lng = 0.0, hasNote = true),
        PlaceUi(
            name = loremIpsumName(words = 2),
            lat = 0.0,
            lng = 0.0,
            hasNote = true,
            attachmentCount = 3,
        ),
        PlaceUi(
            name = loremIpsumName(words = 14),
            lat = 0.0,
            lng = 0.0,
            hasNote = true,
            attachmentCount = 12,
        ),
    )
}

// TODO Move to core

/** A place name [words] words long, for a preview that is about the layout and not about the name. */
private fun loremIpsumName(words: Int): String = LoremIpsum(words).values.first()
//endregion Previews
