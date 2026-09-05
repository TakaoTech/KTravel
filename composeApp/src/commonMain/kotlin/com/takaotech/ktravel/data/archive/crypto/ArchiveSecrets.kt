package com.takaotech.ktravel.data.archive.crypto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The plaintext that gets encrypted into `secrets.json`.
 *
 * An object rather than a bare string so that adding a second credential later changes only this
 * class, not the envelope around it.
 *
 * @property hereApiKey HERE key of the exported plan, empty when the plan had none.
 */
@Serializable
data class ArchiveSecretsPayload(@SerialName("here_api_key") val hereApiKey: String = "")

/**
 * The `secrets.json` entry: the ciphertext plus everything needed to derive the key again.
 *
 * The KDF parameters travel with the archive instead of being read from constants, so tuning them in
 * a later build cannot make already-exported archives unreadable. [scheme] is checked before any
 * decryption is attempted, which is what allows a future scheme — a public-key one, say — to be
 * added without guessing.
 */
@Serializable
data class ArchiveSecretsEnvelope(
    /** How the payload is protected, checked before anything is decrypted. */
    @SerialName("scheme") val scheme: String = SCHEME_SCRYPT_AES256GCM,
    /** Base64, 16 bytes. */
    @SerialName("salt") val salt: String,
    /** scrypt N: CPU/memory cost, a power of two. */
    @SerialName("cost") val cost: Int,
    /** scrypt r: block size. */
    @SerialName("block_size") val blockSize: Int,
    /** scrypt p: parallelisation. */
    @SerialName("parallelization") val parallelization: Int,
    /** Base64, 12 bytes for GCM. */
    @SerialName("nonce") val nonce: String,
    /** Base64 of the encrypted payload. */
    @SerialName("ciphertext") val ciphertext: String,
    /** Base64 GCM tag: this is what makes a wrong password detectable. */
    @SerialName("auth_tag") val authTag: String,
) {
    /** The schemes this build can read. */
    companion object {
        /** scrypt for the key, AES-256-GCM for the payload: the only scheme written so far. */
        const val SCHEME_SCRYPT_AES256GCM: String = "scrypt-aes256gcm-v1"
    }
}
