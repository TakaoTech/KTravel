package com.takaotech.ktravel.data.archive.zip

import kotlinx.io.files.Path

/**
 * Sequential zip writer.
 *
 * Entry paths always use `/` as the separator and are relative to the root of the archive.
 */
interface ZipWriter : AutoCloseable {
    /** Writes [bytes] as the entry [entryPath]. */
    fun writeEntry(entryPath: String, bytes: ByteArray)

    /** Copies [file] as the entry [entryPath] by streaming, without holding it in memory. */
    fun writeEntry(entryPath: String, file: Path)
}

/**
 * Reader over an archive already on disk.
 *
 * The whole index is known as soon as the reader is open, so the guards that reject an archive
 * ([entryPaths], [totalUncompressedSize]) can run before a single byte of content is read.
 */
interface ZipReader : AutoCloseable {
    /** Non-directory entries of the archive, normalised without the leading `/`. */
    fun entryPaths(): Set<String>

    /** Bytes of the entry, or null when it is not there. */
    fun readBytes(entryPath: String): ByteArray?

    /** Total uncompressed size of the entries, used as the zip-bomb guard. */
    fun totalUncompressedSize(): Long

    /** Extracts [entryPath] into [target]; the parent directory has to exist. False when absent. */
    fun extractTo(entryPath: String, target: Path): Boolean
}

/** Opens the archives the export writes and the import reads. */
interface ZipArchiveFactory {
    /** Creates the archive at [archive], truncating whatever is there. */
    fun writer(archive: Path): ZipWriter

    /**
     * Opens [archive] for reading.
     *
     * @throws ZipFormatException when it is not a readable zip, which is how a file the user picked
     * by mistake is told apart from an archive whose content is wrong.
     */
    fun reader(archive: Path): ZipReader
}

/** The archive is not a readable zip. */
class ZipFormatException(message: String, cause: Throwable? = null) : Exception(message, cause)

/** The platform's zip implementation, which is the only place a zip library is named. */
internal expect fun createZipArchiveFactory(): ZipArchiveFactory
