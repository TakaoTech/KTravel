package com.takaotech.ktravel.testutil

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.isDirectory
import io.github.vinceglb.filekit.list
import io.github.vinceglb.filekit.utils.div
import io.kotest.core.TestConfiguration
import kotlinx.io.files.SystemTemporaryDirectory
import kotlin.random.Random

fun TestConfiguration.tempdir(
    prefix: String = "kotest",
    suffix: String = "",
    keepOnFailure: Boolean = false,
): PlatformFile {
    val dirPath = SystemTemporaryDirectory / "${prefix}_${Random.nextLong().toULong()}${suffix}"
    val dir = PlatformFile(dirPath)
    dir.createDirectories(mustCreate = true)

    var hasErrors = false
    afterAny { (_, result) -> if (result.isErrorOrFailure) hasErrors = true }
    afterSpec {
        runCatching {
            if (!keepOnFailure || !hasErrors) dir.deleteRecursively()
        }.onFailure {
            throw TempDirDeletionException(dir)
        }
    }
    return dir
}

private suspend fun PlatformFile.deleteRecursively() {
    if (isDirectory()) list().forEach { it.deleteRecursively() }
    delete(mustExist = false)
}

class TempDirDeletionException(val file: PlatformFile) :
    Exception("Temp dir '$file' could not be deleted")
