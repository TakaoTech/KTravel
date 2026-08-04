@file:OptIn(ExperimentalUuidApi::class)

package com.takaotech.ktravel.data.datasource

import com.takaotech.ktravel.core.io.deleteRecursively
import com.takaotech.ktravel.data.entity.AttachmentEntity
import com.takaotech.ktravel.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.copyTo
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.size
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class AttachmentDataSourceImpl private constructor(
    // Root iniettabile: in produzione deriva da FileKit.filesDir, nei test da una tempdir.
    private val rootProvider: () -> PlatformFile,
) : AttachmentDataSource {

    @Inject
    constructor() : this({ FileKit.filesDir / ATTACHMENTS_DIR })

    /** Costruttore per i test: root esplicita. */
    internal constructor(root: PlatformFile) : this({ root })

    private val root: PlatformFile get() = rootProvider()

    override suspend fun saveAttachment(
        travelId: String,
        stepId: String,
        source: PlatformFile
    ): AttachmentEntity {
        val stepDir = root / travelId / stepId
        stepDir.createDirectories()

        val extension = source.name.substringAfterLast('.', "")
        val fileName = if (extension.isEmpty()) {
            Uuid.random().toString()
        } else {
            "${Uuid.random()}.$extension"
        }

        val destination = stepDir / fileName
        source.copyTo(destination)

        return AttachmentEntity(
            id = Uuid.random().toString(),
            relativePath = "$travelId/$stepId/$fileName",
            originalName = source.name,
            mimeType = mimeTypeFromExtension(extension),
            sizeBytes = destination.size(),
        )
    }

    override fun resolveFile(relativePath: String): PlatformFile =
        relativePath.split('/').fold(root) { dir, segment -> dir / segment }

    override suspend fun deleteAttachment(relativePath: String) {
        resolveFile(relativePath).delete(mustExist = false)
    }

    override suspend fun deleteTravelAttachments(travelId: String) {
        (root / travelId).deleteRecursively()
    }

    companion object {
        const val ATTACHMENTS_DIR = "attachments"
    }
}
