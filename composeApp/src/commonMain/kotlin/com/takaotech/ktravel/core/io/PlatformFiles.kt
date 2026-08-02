package com.takaotech.ktravel.core.io

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.isDirectory
import io.github.vinceglb.filekit.list

/** Elimina file e directory in modo ricorsivo. No-op se il path non esiste. */
suspend fun PlatformFile.deleteRecursively() {
    if (isDirectory()) list().forEach { it.deleteRecursively() }
    delete(mustExist = false)
}
