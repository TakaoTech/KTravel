package com.takaotech.ktravel.core.io

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.isDirectory
import io.github.vinceglb.filekit.list

/**
 * Deletes a file, or a directory with everything under it. Does nothing when the path is gone.
 *
 * Deliberately silent: it runs inside cleanup paths that already log what they were doing, and it
 * has no logger of its own to write through — reaching for the Kermit singleton here is what used to
 * put these lines outside the application's own log.
 */
suspend fun PlatformFile.deleteRecursively() {
    if (isDirectory()) list().forEach { it.deleteRecursively() }
    delete(mustExist = false)
}
