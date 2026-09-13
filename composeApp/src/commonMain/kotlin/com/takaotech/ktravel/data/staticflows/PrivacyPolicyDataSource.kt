package com.takaotech.ktravel.data.staticflows

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.takaotech.ktravel.domain.staticflows.PrivacyPolicy
import ktravel.composeapp.generated.resources.Res

/** Where the privacy policy is packaged, one file per language. */
private const val PRIVACY_DIRECTORY = "files/privacy"

/** What those files are called, before the language. */
private const val PRIVACY_NAME_PREFIX = "privacy_policy_"

/** What those files are called, after the language. */
private const val PRIVACY_EXTENSION = "yaml"

/**
 * Reads the packaged privacy policy, in the language asked for.
 *
 * The policy is written in YAML rather than JSON because almost all of it is Markdown: a `|-` block
 * scalar keeps its paragraphs and lists as they are typed, where a JSON string needs every line break
 * escaped. Keys the model does not know are ignored, as they are for the JSON content.
 *
 * @param readBytes How a packaged file is read. Replaceable so a test can hand over its own content
 *   without going through the resource loader.
 */
class PrivacyPolicyDataSource(readBytes: suspend (String) -> ByteArray = { Res.readBytes(it) }) {

    private val reader = LocalizedContentReader(
        directory = PRIVACY_DIRECTORY,
        namePrefix = PRIVACY_NAME_PREFIX,
        serializer = PrivacyPolicy.serializer(),
        format = Yaml(configuration = YamlConfiguration(strictMode = false)),
        extension = PRIVACY_EXTENSION,
        readBytes = readBytes,
    )

    /**
     * The policy in [language], falling back to English when it has not been translated.
     *
     * @param language A two letter language code, the device's own.
     */
    suspend fun load(language: String): PrivacyPolicy = reader.load(language)
}
