package com.takaotech.ktravel.ui.diagnostics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.core.logging.LogEntry
import com.takaotech.ktravel.core.logging.LogLevel

/**
 * One line of the log: when, how loud, who wrote it and what it says.
 *
 * The stack trace is hidden until the row is tapped — it is the reason the row exists, and also the
 * reason a list of rows would be unreadable if it were always shown.
 *
 * @param entry The line to draw.
 * @param modifier The modifier applied to the row.
 */
@Composable
internal fun LogRow(entry: LogEntry, modifier: Modifier = Modifier) {
    var expanded by remember(entry.id) { mutableStateOf(false) }
    val hasTrace = entry.stackTrace != null

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = hasTrace) { expanded = !expanded }
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag(LogsTestTags.row(entry.id)),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = entry.level.symbol(),
                style = MaterialTheme.typography.labelLarge,
                color = entry.level.color(),
            )
            Text(
                text = entry.tag,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = entry.timestamp.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Text(text = entry.message, style = MaterialTheme.typography.bodyMedium)

        AnimatedVisibility(visible = expanded && hasTrace) {
            Text(
                text = entry.stackTrace.orEmpty(),
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                modifier = Modifier.horizontalScroll(rememberScrollState()),
            )
        }
    }
}

/** The single letter a level is shown as, matching the one used in the exported file. */
internal fun LogLevel.symbol(): String = when (this) {
    LogLevel.Verbose -> "V"
    LogLevel.Debug -> "D"
    LogLevel.Info -> "I"
    LogLevel.Warn -> "W"
    LogLevel.Error -> "E"
    LogLevel.Assert -> "A"
}

/** Errors and warnings are the two a reader is looking for, so they are the two that are coloured. */
@Composable
internal fun LogLevel.color(): Color = when (this) {
    LogLevel.Error, LogLevel.Assert -> MaterialTheme.colorScheme.error
    LogLevel.Warn -> MaterialTheme.colorScheme.tertiary
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}
