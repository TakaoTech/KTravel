package com.takaotech.ktravel.core.io

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.isDirectory
import io.github.vinceglb.filekit.list

/** Deletes a file, or a directory with everything under it. Does nothing when the path is gone. */
suspend fun PlatformFile.deleteRecursively() {
    if (isDirectory()) list().forEach { it.deleteRecursively() }
    delete(mustExist = false)
}
