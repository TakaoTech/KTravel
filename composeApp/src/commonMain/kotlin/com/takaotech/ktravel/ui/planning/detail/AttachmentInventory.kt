package com.takaotech.ktravel.ui.planning.detail

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.presentation.planning.AttachmentUi
import com.takaotech.ktravel.ui.theme.KTravelTheme
import io.github.vinceglb.filekit.PlatformFile
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

internal object AttachmentInventoryTestTags {
    const val SECTION = "step_detail_attachments"
    const val ADD = "step_detail_attachment_add"
    const val ITEM = "step_detail_attachment_item"
}

/**
 * Sezione "inventario file" dello step: caricamento, elenco (miniature per le immagini, riga con
 * icona per i file generici) e azioni per item — inserisci nella nota, apri con l'app di sistema,
 * elimina dall'inventario.
 */
@Composable
internal fun AttachmentInventorySection(
    attachments: List<AttachmentUi>,
    resolveFile: (String) -> PlatformFile,
    isEditing: Boolean,
    onAdd: () -> Unit,
    onInsert: (AttachmentUi) -> Unit,
    onOpen: (AttachmentUi) -> Unit,
    onRemove: (AttachmentUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag(AttachmentInventoryTestTags.SECTION),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(Res.string.planning_detail_attachments_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                modifier = Modifier.testTag(AttachmentInventoryTestTags.ADD),
                onClick = onAdd
            ) {
                Icon(
                    painter = painterResource(Res.drawable.add),
                    contentDescription = stringResource(Res.string.planning_detail_cd_add_attachment)
                )
            }
        }

        if (attachments.isEmpty()) {
            Text(
                text = stringResource(Res.string.planning_detail_attachments_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(attachments) { attachment ->
                    AttachmentRow(
                        attachment = attachment,
                        isEditing = isEditing,
                        resolveFile = resolveFile,
                        onInsert = { onInsert(attachment) },
                        onOpen = { onOpen(attachment) },
                        onRemove = { onRemove(attachment) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AttachmentRow(
    attachment: AttachmentUi,
    isEditing: Boolean,
    resolveFile: (String) -> PlatformFile,
    onInsert: () -> Unit,
    onOpen: () -> Unit,
    onRemove: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().testTag(AttachmentInventoryTestTags.ITEM),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AttachmentThumbnail(attachment = attachment, resolveFile = resolveFile)

        Text(
            text = attachment.originalName,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        if (isEditing) {
            IconButton(onClick = onInsert) {
                Icon(
                    painter = painterResource(Res.drawable.arrow_right_alt),
                    contentDescription = stringResource(Res.string.planning_detail_cd_insert_attachment)
                )
            }
        }


        IconButton(onClick = onOpen) {
            Icon(
                painter = painterResource(Res.drawable.open_in_new),
                contentDescription = stringResource(Res.string.planning_detail_cd_open_attachment)
            )
        }
        IconButton(onClick = onRemove) {
            Icon(
                painter = painterResource(Res.drawable.delete),
                contentDescription = stringResource(Res.string.planning_detail_cd_remove_attachment)
            )
        }
    }
}

@Composable
private fun AttachmentThumbnail(
    attachment: AttachmentUi,
    resolveFile: (String) -> PlatformFile,
) {
    // In preview i file reali non esistono: si evita la lettura da disco e si mostra un placeholder.
    val isPreview = LocalInspectionMode.current
    Surface(
        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Box(contentAlignment = Alignment.Center) {
            when {
                attachment.isImage && !isPreview -> {
                    when (val image =
                        rememberAttachmentImage(resolveFile(attachment.relativePath))) {
                        is AttachmentImage.Loaded -> Image(
                            painter = image.painter,
                            contentDescription = attachment.originalName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(48.dp)
                        )

                        AttachmentImage.Error -> Icon(
                            painter = painterResource(Res.drawable.error),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(12.dp)
                        )

                        AttachmentImage.Loading -> Unit
                    }
                }

                else -> Icon(
                    painter = painterResource(Res.drawable.description),
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

@Preview
@Composable
private fun AttachmentInventorySectionPreview() = KTravelTheme {
    AttachmentInventorySection(
        attachments = listOf(
            AttachmentUi(
                id = "a1",
                relativePath = "t1/s1/tokyo.jpg",
                originalName = "tokyo-tower.jpg",
                mimeType = "image/jpeg",
                isImage = true
            ),
            AttachmentUi(
                id = "a2",
                relativePath = "t1/s1/ticket.pdf",
                originalName = "ticket.pdf",
                mimeType = "application/pdf",
                isImage = false
            )
        ),
        resolveFile = { PlatformFile(Path(it)) },
        onAdd = {},
        onInsert = {},
        onOpen = {},
        onRemove = {},
        isEditing = true
    )
}

@Preview
@Composable
private fun AttachmentInventorySectionEmptyPreview() = KTravelTheme {
    AttachmentInventorySection(
        attachments = emptyList(),
        resolveFile = { PlatformFile(Path(it)) },
        onAdd = {},
        onInsert = {},
        onOpen = {},
        onRemove = {},
        isEditing = true
    )
}
