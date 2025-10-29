package com.takaotech.ktravel.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DatePicker
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.check
import ktravel.composeapp.generated.resources.close
import ktravel.composeapp.generated.resources.edit
import org.jetbrains.compose.resources.painterResource
import kotlin.random.Random

@Composable
fun TDaySection(
    day: String,
    modifier: Modifier = Modifier,
    isOpen: Boolean,
    onDayCollapseClicked: () -> Unit,
) {
    Column(
        modifier = modifier
    ) {
        TextButton(
            onClick = onDayCollapseClicked,
        ) {
            Text(text = day)
        }

        AnimatedVisibility(visible = isOpen) {
            Column(modifier = Modifier.padding(start = 32.dp)) {
                Text(text = "test")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TDaySectionPreview() {
    var isOpen by remember { mutableStateOf(true) }

    TDaySection(
        day = "21 giugno 2025",
        isOpen = isOpen,
        onDayCollapseClicked = {
            isOpen = !isOpen
        }
    )
}