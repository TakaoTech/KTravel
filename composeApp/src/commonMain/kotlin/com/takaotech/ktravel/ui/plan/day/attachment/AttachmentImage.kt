package com.takaotech.ktravel.ui.plan.day.attachment

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.size.Precision
import com.mikepenz.markdown.model.ImageData
import com.mikepenz.markdown.model.ImageTransformer
import com.takaotech.ktravel.domain.model.AttachmentReference
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.path
import okio.Path.Companion.toPath

/**
 * Longest side an image embedded in a note is decoded at.
 *
 * A note is read on a screen, not printed: decoding a camera photo at its own resolution costs tens
 * of megabytes and the time to produce them, for pixels no display shows.
 */
private const val NOTE_IMAGE_MAX_PX = 1280

/**
 * The Coil model of an inventory file: its path on disk, which Coil maps to a `file://` request on
 * every platform.
 *
 * Loading through Coil rather than reading and decoding by hand is what keeps the decode off the UI
 * thread, shares one memory cache between the inventory thumbnails and the images embedded in a
 * note, and decodes at the size actually drawn instead of the size the file happens to have.
 */
internal fun PlatformFile.asCoilModel(): Any = path.toPath()

/**
 * Builds the request for an inventory image, bounded to [maxSizePx] on its longest side.
 *
 * The bound is explicit rather than resolved from the draw scope: a painter whose size is only
 * known once it is drawn reports no intrinsic size until then, and both callers here lay out around
 * that size.
 *
 * [contentScale] must be the one the caller draws with: Coil derives the decode scale from it, so a
 * painter decoded to fit a box and then drawn cropped to fill it is upscaled from too few pixels.
 */
@Composable
internal fun rememberAttachmentImagePainter(
    file: PlatformFile,
    maxSizePx: Int,
    contentScale: ContentScale = ContentScale.Fit,
    errorPainter: Painter? = null,
): AsyncImagePainter = rememberAsyncImagePainter(
    model = ImageRequest.Builder(LocalPlatformContext.current)
        .data(file.asCoilModel())
        .size(maxSizePx)
        .precision(Precision.INEXACT)
        .build(),
    error = errorPainter,
    contentScale = contentScale,
)

/**
 * Resolves the `ktravel://attachment/<rel>` links a note may carry into the files behind them.
 *
 * A reference that resolves to nothing — a file removed from the inventory after the note was
 * written — draws [errorPainter] rather than disappearing, so the note still says that something
 * was meant to be there. Any other URL is left alone: a note never reaches out to the network.
 */
class AttachmentImageTransformer(
    private val resolveFile: (String) -> PlatformFile,
    private val errorPainter: Painter,
) : ImageTransformer {

    @Composable
    override fun transform(link: String): ImageData? {
        val relativePath = AttachmentReference.relativePathOf(link) ?: return null
        return ImageData(
            painter = rememberAttachmentImagePainter(
                file = resolveFile(relativePath),
                maxSizePx = NOTE_IMAGE_MAX_PX,
                errorPainter = errorPainter,
            ),
        )
    }
}
