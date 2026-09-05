package com.takaotech.ktravel.ui.plan.day.attachment

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import com.takaotech.ktravel.core.data.mime.MimeType
import com.takaotech.ktravel.presentation.plan.AttachmentUi
import com.takaotech.ktravel.ui.plan.day.notes.StepNotesTestTags
import com.takaotech.ktravel.ui.plan.day.placestep.PlaceStepTestTags
import com.takaotech.ktravel.ui.theme.KTravelTheme
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.io.files.Path
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.add
import ktravel.composeapp.generated.resources.arrow_right_alt
import ktravel.composeapp.generated.resources.delete
import ktravel.composeapp.generated.resources.description
import ktravel.composeapp.generated.resources.error
import ktravel.composeapp.generated.resources.open_in_new
import ktravel.composeapp.generated.resources.planning_detail_attachments_empty
import ktravel.composeapp.generated.resources.planning_detail_attachments_title
import ktravel.composeapp.generated.resources.planning_detail_cd_add_attachment
import ktravel.composeapp.generated.resources.planning_detail_cd_insert_attachment
import ktravel.composeapp.generated.resources.planning_detail_cd_open_attachment
import ktravel.composeapp.generated.resources.planning_detail_cd_remove_attachment
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * The sheet's drag handle: a 4.dp bar inside 22.dp of padding above and below it.
 */
internal val DRAG_HANDLE_HEIGHT = 48.dp

/** The header row is as tall as the minimum touch target of the add button it carries. */
internal val HEADER_ROW_HEIGHT = 48.dp

/**
 * How much of a sheet hosting [AttachmentInventorySection] has to stay visible when it is closed.
 *
 * `BottomSheetDefaults.SheetPeekHeight` is 56.dp, which the drag handle alone very nearly fills:
 * the closed sheet showed a bare strip, with the "Files" title and the add button cut off below the
 * fold, so nothing on screen said what pulling it up would reveal. This is the handle plus one
 * header row.
 */
internal val ATTACHMENT_INVENTORY_PEEK_HEIGHT = DRAG_HANDLE_HEIGHT + HEADER_ROW_HEIGHT

/**
 * The files attached to a step: adding them, listing them, and what can be done with each.
 *
 * Images are listed as thumbnails and everything else as a row with an icon, and each entry can be
 * referenced from the note, opened with the platform's own application, or removed.
 *
 * Every detail screen that carries notes hosts this same section, so the test tags are named by the
 * host through [testTags] rather than fixed here.
 */
@Composable
internal fun AttachmentInventorySection(
    attachments: ImmutableList<AttachmentUi>,
    resolveFile: (String) -> PlatformFile,
    isEditing: Boolean,
    testTags: StepNotesTestTags,
    onAdd: () -> Unit,
    onInsert: (AttachmentUi) -> Unit,
    onOpen: (AttachmentUi) -> Unit,
    onRemove: (AttachmentUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTags.attachments)
            .padding(start = 8.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(Res.string.planning_detail_attachments_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            IconButton(
                modifier = Modifier.testTag(testTags.attachmentAdd),
                onClick = onAdd,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.add),
                    contentDescription = stringResource(Res.string.planning_detail_cd_add_attachment),
                )
            }
        }

        if (attachments.isEmpty()) {
            Text(
                text = stringResource(Res.string.planning_detail_attachments_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(attachments, key = { it.id }) { attachment ->
                    AttachmentRow(
                        attachment = attachment,
                        isEditing = isEditing,
                        resolveFile = resolveFile,
                        itemTestTag = testTags.attachmentItem,
                        onInsert = { onInsert(attachment) },
                        onOpen = { onOpen(attachment) },
                        onRemove = { onRemove(attachment) },
                    )
                }
            }
        }
    }
}

@Composable
internal fun AttachmentRow(
    attachment: AttachmentUi,
    isEditing: Boolean,
    resolveFile: (String) -> PlatformFile,
    itemTestTag: String,
    onInsert: () -> Unit,
    onOpen: () -> Unit,
    onRemove: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().testTag(itemTestTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AttachmentThumbnail(attachment = attachment, resolveFile = resolveFile)

        Text(
            text = attachment.originalName,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )

        if (isEditing) {
            IconButton(onClick = onInsert) {
                Icon(
                    painter = painterResource(Res.drawable.arrow_right_alt),
                    contentDescription = stringResource(Res.string.planning_detail_cd_insert_attachment),
                )
            }
        }

        IconButton(onClick = onOpen) {
            Icon(
                painter = painterResource(Res.drawable.open_in_new),
                contentDescription = stringResource(Res.string.planning_detail_cd_open_attachment),
            )
        }
        IconButton(onClick = onRemove) {
            Icon(
                painter = painterResource(Res.drawable.delete),
                contentDescription = stringResource(Res.string.planning_detail_cd_remove_attachment),
            )
        }
    }
}

/** Side of an inventory thumbnail, and the size its image is decoded at. */
internal val THUMBNAIL_SIZE = 48.dp

@Composable
internal fun AttachmentThumbnail(attachment: AttachmentUi, resolveFile: (String) -> PlatformFile) {
    val isPreview = LocalInspectionMode.current
    Surface(
        modifier = Modifier.size(THUMBNAIL_SIZE).clip(RoundedCornerShape(8.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Box(contentAlignment = Alignment.Center) {
            when {
                attachment.isImage && !isPreview -> {
                    val thumbnailPx = with(LocalDensity.current) { THUMBNAIL_SIZE.roundToPx() }
                    val painter = rememberAttachmentImagePainter(
                        file = resolveFile(attachment.relativePath),
                        maxSizePx = thumbnailPx,
                        // Same scale the Image below draws with, or the decode is too small for it.
                        contentScale = ContentScale.Crop,
                    )

                    when (painter.state.collectAsState().value) {
                        is AsyncImagePainter.State.Success -> Image(
                            painter = painter,
                            contentDescription = attachment.originalName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(THUMBNAIL_SIZE),
                        )

                        is AsyncImagePainter.State.Error -> Icon(
                            painter = painterResource(Res.drawable.error),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(12.dp),
                        )

                        else -> Unit
                    }
                }

                else -> Icon(
                    painter = painterResource(Res.drawable.description),
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp),
                )
            }
        }
    }
}

//region Previews

@Preview
@Composable
private fun AttachmentInventorySectionPreview() = KTravelTheme {
    Surface {
        AttachmentInventorySection(
            attachments = persistentListOf(
                AttachmentUi(
                    id = "a1",
                    relativePath = "t1/s1/tokyo.jpg",
                    originalName = "tokyo-tower.jpg",
                    mimeType = MimeType("image/jpeg"),
                ),
                AttachmentUi(
                    id = "a2",
                    relativePath = "t1/s1/ticket.pdf",
                    originalName = "ticket.pdf",
                    mimeType = MimeType("application/pdf"),
                ),
            ),
            resolveFile = { PlatformFile(Path(it)) },
            testTags = PlaceStepTestTags.NOTES,
            onAdd = {},
            onInsert = {},
            onOpen = {},
            onRemove = {},
            isEditing = true,
        )
    }
}

@Preview
@Composable
private fun AttachmentInventorySectionEmptyPreview() = KTravelTheme {
    Surface {
        AttachmentInventorySection(
            attachments = persistentListOf(),
            resolveFile = { PlatformFile(Path(it)) },
            testTags = PlaceStepTestTags.NOTES,
            onAdd = {},
            onInsert = {},
            onOpen = {},
            onRemove = {},
            isEditing = true,
        )
    }
}
//endregion Preview
