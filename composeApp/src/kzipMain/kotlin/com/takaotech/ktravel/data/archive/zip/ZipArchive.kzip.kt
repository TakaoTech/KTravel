package com.takaotech.ktravel.data.archive.zip

import de.jonasbroeckmann.kzip.Zip
import de.jonasbroeckmann.kzip.open
import kotlinx.io.Buffer
import kotlinx.io.files.Path

/**
 * Implementazione basata su kzip, condivisa da Android, Desktop e iOS via `kotlin.srcDir`.
 *
 * kzip non pubblica un artefatto per il target androidJvm: Android e Desktop usano `kzip-jvm`,
 * iOS il modulo multipiattaforma. L'API è la stessa, quindi il sorgente è unico.
 */
internal actual fun createZipArchiveFactory(): ZipArchiveFactory = KzipArchiveFactory

private object KzipArchiveFactory : ZipArchiveFactory {

    override fun writer(archive: Path): ZipWriter = KzipWriter(Zip.open(archive, mode = Zip.Mode.Write))

    override fun reader(archive: Path): ZipReader {
        val zip = Zip.open(archive, mode = Zip.Mode.Read)
        // kzip legge la central directory in modo lazy: l'indice va costruito qui dentro, altrimenti
        // un file che non è uno zip fallirebbe molto più tardi con un'eccezione non mappata.
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
     * Indice path -> posizione: il lookup per indice evita di dipendere da come ogni piattaforma
     * normalizza i [Path] usati come chiave.
     */
    private val entryIndex = mutableMapOf<String, Int>()
    private var totalSize = 0L

    init {
        // Loop esplicito invece di `forEachEntryIndexed`: le funzioni inline di kzip sono compilate
        // con JVM target 22 e non sono inlinabili nel bytecode 21 del progetto.
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
