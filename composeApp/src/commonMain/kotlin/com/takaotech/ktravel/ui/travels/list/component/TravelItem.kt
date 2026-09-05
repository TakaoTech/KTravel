package com.takaotech.ktravel.ui.travels.list.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics

@Composable
internal fun TravelItem(
    name: String,
    startDate: String?,
    endDate: String?,
    modifier: Modifier = Modifier,
    isSelectionMode: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onSwipeToDelete: () -> Unit,
) {
    val itemModifier = modifier.semantics { this.selected = selected }

    if (isSelectionMode) {
        // Swiping is disabled while picking items, otherwise the two gestures would fight each other.
        TravelItemCard(
            modifier = itemModifier,
            name = name,
            startDate = startDate,
            endDate = endDate,
            isSelectionMode = true,
            selected = selected,
            onClick = onClick,
            onLongClick = onLongClick,
        )
    } else {
        SwipeToDeleteBox(
            modifier = itemModifier,
            onSwipeToDelete = onSwipeToDelete,
        ) {
            TravelItemCard(
                modifier = Modifier.fillMaxWidth(),
                name = name,
                startDate = startDate,
                endDate = endDate,
                isSelectionMode = false,
                selected = selected,
                onClick = onClick,
                onLongClick = onLongClick,
            )
        }
    }
}
