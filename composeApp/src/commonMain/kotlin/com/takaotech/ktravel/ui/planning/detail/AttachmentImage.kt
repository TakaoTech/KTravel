package com.takaotech.ktravel.ui.planning.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.decodeToImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import com.mikepenz.markdown.model.ImageData
import com.mikepenz.markdown.model.ImageTransformer
import com.takaotech.ktravel.domain.model.AttachmentReference
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes

/** Stato di caricamento di un'immagine dell'inventario da disco. */
sealed interface AttachmentImage {
    data object Loading : AttachmentImage
    data object Error : AttachmentImage
    data class Loaded(val painter: Painter) : AttachmentImage
}

/**
 * Carica un'immagine locale ([PlatformFile]) leggendone i byte e decodificandoli con l'API nativa di
 * Compose (`decodeToImageBitmap`), senza dipendere da Coil né dalla sua configurazione globale.
 * Un file mancante o non decodificabile diventa [AttachmentImage.Error].
 */
@Composable
fun rememberAttachmentImage(file: PlatformFile): AttachmentImage =
    produceState<AttachmentImage>(AttachmentImage.Loading, file) {
        value = runCatching {
            AttachmentImage.Loaded(BitmapPainter(file.readBytes().decodeToImageBitmap()))
        }.getOrElse { AttachmentImage.Error }
    }.value

/**
 * [ImageTransformer] per mikepenz che risolve gli URL `ktravel://attachment/<rel>` in immagini locali.
 * Un riferimento non risolvibile (file assente dall'inventario) rende l'[errorPainter] come marker
 * d'errore visibile; gli URL non-allegato vengono ignorati (nessun caricamento remoto).
 */
class AttachmentImageTransformer(
    private val resolveFile: (String) -> PlatformFile,
    private val errorPainter: Painter,
) : ImageTransformer {

    @Composable
    override fun transform(link: String): ImageData? {
        val relativePath = AttachmentReference.relativePathOf(link) ?: return null
        return when (val image = rememberAttachmentImage(resolveFile(relativePath))) {
            AttachmentImage.Loading -> null
            AttachmentImage.Error -> ImageData(painter = errorPainter)
            is AttachmentImage.Loaded -> ImageData(painter = image.painter)
        }
    }
}
