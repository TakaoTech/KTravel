package com.takaotech.ktravel.data.archive.zip

import de.jonasbroeckmann.kzip.Zip
import de.jonasbroeckmann.kzip.open
import kotlinx.io.Buffer
import kotlinx.io.files.Path

/**
 * Implementation on top of kzip, shared by Android, Desktop and iOS through `kotlin.srcDir`.
 *
 * kzip publishes no artifact for the androidJvm target: Android and Desktop take `kzip-jvm`, iOS
 * the multiplatform module. The API is the same on both, so the source is written once.
 */
internal actual fun createZipArchiveFactory(): ZipArchiveFactory = KzipArchiveFactory

private object KzipArchiveFactory : ZipArchiveFactory {

    override fun writer(archive: Path): ZipWriter = KzipWriter(Zip.open(archive, mode = Zip.Mode.Write))

    override fun reader(archive: Path): ZipReader {
        val zip = Zip.open(archive, mode = Zip.Mode.Read)
        // kzip reads the central directory lazily: the index has to be built inside this try,
        // otherwise a file that is not a zip would fail much later with an unmapped exception.
        return try {
            KzipReader(zip)
        } catch (throwable: Throwable) {
            runCatching { zip.close() }
            throw ZipFormatException("Not a readable zip archive: $archive", throwable)
        }
    }
}

private class KzipWriter(private val zip: Zip) : ZipWriter {

    override fun writeEntry(entryPath: String, bytes: ByteArray) {
        zip.entryFromSource(Path(entryPath), Buffer().apply { write(bytes) })
    }

    override fun writeEntry(entryPath: String, file: Path) {
        zip.entryFromPath(Path(entryPath), file)
    }

    override fun close() = zip.close()
}

private class KzipReader(private val zip: Zip) : ZipReader {

    /**
     * Index from path to position: looking an entry up by index avoids depending on how each
     * platform normalises the [Path] used as a key.
     */
    private val entryIndex = mutableMapOf<String, Int>()
    private var totalSize = 0L

    init {
        // Explicit loop instead of `forEachEntryIndexed`: kzip's inline functions are compiled for
        // JVM target 22 and cannot be inlined into this project's bytecode 21.
        for (index in 0 until zip.numberOfEntries) {
            zip.entry(index) {
                if (!isDirectory) {
                    entryIndex[path.toString().normalizeEntryPath()] = index
                    totalSize += uncompressedSize.toLong()
                }
            }
        }
    }

    override fun entryPaths(): Set<String> = entryIndex.keys

    override fun readBytes(entryPath: String): ByteArray? =
        entryIndex[entryPath]?.let { index -> zip.entry(index) { readToBytes() } }

    override fun totalUncompressedSize(): Long = totalSize

    override fun extractTo(entryPath: String, target: Path): Boolean {
        val index = entryIndex[entryPath] ?: return false
        zip.entry(index) { readToPath(target) }
        return true
    }

    override fun close() = zip.close()
}

private fun String.normalizeEntryPath(): String = replace('\\', '/').removePrefix("/")
