package com.takaotech.ktravel.data.staticflows

import com.takaotech.ktravel.domain.staticflows.PrivacyPolicy
import ktravel.composeapp.generated.resources.Res

/** Where the privacy policy is packaged, one file per language. */
private const val PRIVACY_DIRECTORY = "files/privacy"

/** What those files are called, before the language. */
private const val PRIVACY_NAME_PREFIX = "privacy_policy_"

/**
 * Reads the packaged privacy policy, in the language asked for.
 *
 * @param readBytes How a packaged file is read. Replaceable so a test can hand over its own content
 *   without going through the resource loader.
 */
class PrivacyPolicyDataSource(readBytes: suspend (String) -> ByteArray = { Res.readBytes(it) }) {

    private val reader = LocalizedContentReader(
        directory = PRIVACY_DIRECTORY,
        namePrefix = PRIVACY_NAME_PREFIX,
        serializer = PrivacyPolicy.serializer(),
        readBytes = readBytes,
    )

    /**
     * The policy in [language], falling back to English when it has not been translated.
     *
     * @param language A two letter language code, the device's own.
     */
    suspend fun load(language: String): PrivacyPolicy = reader.load(language)
}
