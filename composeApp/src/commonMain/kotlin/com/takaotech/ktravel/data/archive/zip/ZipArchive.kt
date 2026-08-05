package com.takaotech.ktravel.data.archive.zip

import kotlinx.io.files.Path

/**
 * Scrittore zip sequenziale.
 *
 * Le entry path usano sempre `/` come separatore e sono relative alla radice dell'archivio.
 */
interface ZipWriter : AutoCloseable {
    fun writeEntry(entryPath: String, bytes: ByteArray)

    /** Copia [file] come entry [entryPath] in streaming, senza caricarlo in memoria. */
    fun writeEntry(entryPath: String, file: Path)
}

interface ZipReader : AutoCloseable {
    /** Entry non-directory presenti nell'archivio, normalizzate senza `/` iniziale. */
    fun entryPaths(): Set<String>

    /** Byte della entry, o null se assente. */
    fun readBytes(entryPath: String): ByteArray?

    /** Dimensione non compressa complessiva delle entry, usata come guardia anti zip-bomb. */
    fun totalUncompressedSize(): Long

    /** Estrae [entryPath] in [target]; la directory padre deve esistere. False se la entry manca. */
    fun extractTo(entryPath: String, target: Path): Boolean
}

interface ZipArchiveFactory {
    /** Crea (troncando) l'archivio in [archive]. */
    fun writer(archive: Path): ZipWriter

    /** Apre [archive] in lettura. Lancia [ZipFormatException] se non è uno zip leggibile. */
    fun reader(archive: Path): ZipReader
}

/** L'archivio non è uno zip leggibile. */
class ZipFormatException(message: String, cause: Throwable? = null) : Exception(message, cause)

internal expect fun createZipArchiveFactory(): ZipArchiveFactory
