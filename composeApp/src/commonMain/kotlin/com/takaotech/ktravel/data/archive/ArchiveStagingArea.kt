@file:OptIn(ExperimentalUuidApi::class, ExperimentalAtomicApi::class)

package com.takaotech.ktravel.data.archive

import com.takaotech.ktravel.core.io.deleteRecursively
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.list
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Area di lavoro su filesystem reale usata da export e import.
 *
 * Serve perché l'astrazione zip lavora su path del filesystem, mentre i file scelti dall'utente su
 * Android sono `content://` non convertibili in path.
 *
 * Export e import cancellano sempre la propria directory, ma un archivio in attesa di conferma
 * sopravvive alla morte del processo: la prima operazione dopo l'avvio ripulisce quindi i residui.
 */
internal class ArchiveStagingArea(private val rootProvider: () -> PlatformFile) {

    /** Crea una directory di lavoro esclusiva, ripulendo una volta sola i residui di sessioni morte. */
    suspend fun newSession(): PlatformFile {
        val root = rootProvider()
        if (leftoversCleaned.compareAndSet(false, true)) {
            runCatching {
                if (root.exists()) root.list().forEach { it.deleteRecursively() }
            }
        }
        return (root / Uuid.random().toString()).also { it.createDirectories() }
    }

    private companion object {
        /** Il flag è di processo: le sessioni della stessa esecuzione non si cancellano a vicenda. */
        val leftoversCleaned = AtomicBoolean(false)
    }
}
