package com.takaotech.ktravel.data.staticflows

import com.takaotech.ktravel.domain.staticflows.IntroFlow
import ktravel.composeapp.generated.resources.Res

/** Where the introduction is packaged, one file per language. */
private const val INTRO_DIRECTORY = "files/intro"

/** What those files are called, before the language. */
private const val INTRO_NAME_PREFIX = "intro_flow_"

/**
 * Reads the packaged introduction, in the language asked for.
 *
 * @param readBytes How a packaged file is read. Replaceable so a test can hand over its own content
 *   without going through the resource loader.
 */
class IntroFlowDataSource(readBytes: suspend (String) -> ByteArray = { Res.readBytes(it) }) {

    private val reader = LocalizedContentReader(
        directory = INTRO_DIRECTORY,
        namePrefix = INTRO_NAME_PREFIX,
        serializer = IntroFlow.serializer(),
        readBytes = readBytes,
    )

    /**
     * The introduction in [language], falling back to English when it has not been translated.
     *
     * @param language A two letter language code, the device's own.
     */
    suspend fun load(language: String): IntroFlow = reader.load(language)
}
