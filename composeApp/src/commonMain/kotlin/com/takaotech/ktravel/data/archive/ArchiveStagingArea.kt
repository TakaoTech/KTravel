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
 * Real filesystem workspace shared by export and import.
 *
 * It exists because the zip abstraction works on filesystem paths, while the files the user picks on
 * Android are `content://` uris that cannot be turned into one: everything is copied here first.
 *
 * Export and import always delete their own directory, but a staged archive waiting for the user to
 * confirm the import outlives the death of the process, so the first session after startup also
 * sweeps away what previous runs left behind.
 */
internal class ArchiveStagingArea(private val rootProvider: () -> PlatformFile) {

    /**
     * Creates an exclusive working directory under the root, sweeping the leftovers of dead sessions
     * once per process.
     *
     * The sweep runs before the new directory exists, so it can delete every child of the root
     * without having to tell live sessions from dead ones. It is best effort on purpose: a leftover
     * that cannot be removed (a file still locked, a permission lost) must not fail the export or
     * import that is only passing through here.
     */
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
        /**
         * Process wide flag: sessions of the same run never delete each other.
         *
         * It is shared by every instance, which is correct as long as they all stage under the same
         * root — as export and import do. An area rooted elsewhere would be swept only if it happens
         * to open the first session of the process.
         */
        val leftoversCleaned = AtomicBoolean(false)
    }
}
