package com.takaotech.ktravel.ui.intro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/** How far apart the blocks of a designed step sit: the badge, the text, the boxes, the notes. */
internal val SECTION_SPACING = 24.dp

/** How far apart two lines of the same block sit. */
internal val BLOCK_SPACING = 10.dp

/** How much room an outlined box keeps inside its own hairline. */
internal val CARD_HORIZONTAL_PADDING = 16.dp
internal val CARD_VERTICAL_PADDING = 14.dp

/** How far the control of an outlined box sits from the text beside it. */
internal val CARD_CONTENT_SPACING = 12.dp

/** How thick the hairline around an outlined container is drawn. */
internal val OUTLINE_WIDTH = 1.dp

/** The badge a step opens on, and the icon inside it. */
private val BADGE_SIZE = 64.dp
private val BADGE_ICON_SIZE = 32.dp
private val BADGE_CORNER = 20.dp

/** How wide the letters of an overline are set. */
private val OVERLINE_TRACKING = 1.2.sp

/** The badge a step opens on: the one picture a page of text is allowed. */
@Composable
internal fun IntroBadge(
    icon: DrawableResource,
    modifier: Modifier = Modifier,
    container: Color = MaterialTheme.colorScheme.primaryContainer,
    content: Color = MaterialTheme.colorScheme.onPrimaryContainer,
) {
    Box(
        modifier = modifier
            .size(BADGE_SIZE)
            .clip(RoundedCornerShape(BADGE_CORNER))
            .background(container),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(BADGE_ICON_SIZE),
            tint = content,
        )
    }
}

/** What a designed step opens with: the overline, the heading and the paragraph under them. */
@Composable
internal fun IntroStepHeading(overline: String, title: String, body: String?, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(BLOCK_SPACING)) {
        Text(
            text = overline.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = OVERLINE_TRACKING,
            color = MaterialTheme.colorScheme.primary,
        )

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
        )

        body?.let {
            Markdown(
                content = it,
                colors = markdownColor(text = MaterialTheme.colorScheme.onSurfaceVariant),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
