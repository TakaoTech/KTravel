package com.takaotech.ktravel.data.staticflows

import kotlinx.serialization.KSerializer
import kotlinx.serialization.StringFormat
import kotlinx.serialization.json.Json
import ktravel.composeapp.generated.resources.Res

/** The language every installation can count on: the file that must exist. */
const val DEFAULT_CONTENT_LANGUAGE: String = "en"

/** The format content is read with when nothing else is asked for: JSON, tolerant of keys it does not know. */
private val lenientJson = Json { ignoreUnknownKeys = true }

/**
 * Reads one piece of packaged, translated content out of the resources.
 *
 * Compose Resources applies its qualifiers to `values`, `drawable` and `font`, not to the raw files
 * under `files`, so the language is resolved here: the file for the device's language when it has
 * been translated, and the English one otherwise.
 *
 * @param T What the file decodes to.
 * @param directory Where the files live, e.g. `files/intro`.
 * @param namePrefix What their names start with; the language and [extension] complete them.
 * @param serializer How the file is decoded.
 * @param format The format the files are written in. JSON by default; content whose text is long
 *   Markdown is easier to edit in YAML, where a block scalar keeps its line breaks as they are.
 * @param extension The extension of the files, without the dot. Has to match [format].
 * @param readBytes How a packaged file is read. Replaceable so a test can hand over its own content
 *   without going through the resource loader.
 */
class LocalizedContentReader<T>(
    private val directory: String,
    private val namePrefix: String,
    private val serializer: KSerializer<T>,
    private val format: StringFormat = lenientJson,
    private val extension: String = "json",
    private val readBytes: suspend (String) -> ByteArray = { Res.readBytes(it) },
) {

    /**
     * The content in [language], falling back to English when it has not been translated.
     *
     * @param language A two letter language code, the device's own.
     * @throws IllegalStateException when even the English file is missing, which means the
     *   application was packaged without content it cannot start without.
     */
    suspend fun load(language: String): T = read(language)
        ?: read(DEFAULT_CONTENT_LANGUAGE)
        ?: error("${fileName(DEFAULT_CONTENT_LANGUAGE)} is missing")

    private suspend fun read(language: String): T? = runCatching {
        format.decodeFromString(serializer, readBytes(fileName(language)).decodeToString())
    }.getOrNull()

    private fun fileName(language: String): String = "$directory/$namePrefix$language.$extension"
}
