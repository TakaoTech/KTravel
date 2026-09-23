package com.takaotech.ktravel.ui.plan.place.component

import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.takaotech.ktravel.ui.theme.KTravelTheme
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.check
import ktravel.composeapp.generated.resources.checklist_24dp
import ktravel.composeapp.generated.resources.close
import ktravel.composeapp.generated.resources.place_insert_cd_close
import ktravel.composeapp.generated.resources.place_insert_cd_confirm
import ktravel.composeapp.generated.resources.place_insert_cd_inventory
import ktravel.composeapp.generated.resources.place_insert_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * The bar over the «add places» screen: close on the left; on the right the inventory of the
 * selection, with its count, and the confirmation that adds it to the trip.
 *
 * @param selectedCount How many places are selected, shown as a badge on the inventory button.
 * @param isInventoryOpen Draws the inventory button as checked while the inventory is showing.
 * @param canConfirm Enables the confirmation, which is filled while there is something to add.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PlaceInsertTopBar(
    selectedCount: Int,
    isInventoryOpen: Boolean,
    canConfirm: Boolean,
    onClose: () -> Unit,
    onInventoryClick: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        modifier = modifier,
        title = { Text(stringResource(Res.string.place_insert_title)) },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(
                    painter = painterResource(Res.drawable.close),
                    contentDescription = stringResource(Res.string.place_insert_cd_close),
                )
            }
        },
        actions = {
            FilledIconToggleButton(
                checked = isInventoryOpen,
                onCheckedChange = { onInventoryClick() },
            ) {
                BadgedBox(badge = { if (selectedCount > 0) Badge { Text(selectedCount.toString()) } }) {
                    Icon(
                        painter = painterResource(Res.drawable.checklist_24dp),
                        contentDescription = stringResource(Res.string.place_insert_cd_inventory),
                    )
                }
            }
            val confirmIcon: @Composable () -> Unit = {
                Icon(
                    painter = painterResource(Res.drawable.check),
                    contentDescription = stringResource(Res.string.place_insert_cd_confirm),
                )
            }

            IconButton(onClick = onConfirm, enabled = canConfirm, content = confirmIcon)
        },
    )
}

//region Previews
@Preview(showBackground = true)
@Composable
private fun PlaceInsertTopBarPreview() = KTravelTheme {
    Surface {
        PlaceInsertTopBar(
            selectedCount = 3,
            isInventoryOpen = true,
            canConfirm = true,
            onClose = {},
            onInventoryClick = {},
            onConfirm = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PlaceInsertTopBarEmptyPreview() = KTravelTheme {
    Surface {
        PlaceInsertTopBar(
            selectedCount = 0,
            isInventoryOpen = false,
            canConfirm = false,
            onClose = {},
            onInventoryClick = {},
            onConfirm = {},
        )
    }
}
//endregion Previews
