@file:OptIn(ExperimentalEncodingApi::class)
// The sealed-box API is spread across top-level extensions (`nonce`, `authTag`, `sealedBox`, `from`,
// `keyFrom`) resolved by receiver type; naming them one by one breaks whenever an overload resolves
// differently, so the wildcard is deliberate.
@file:Suppress("WildcardImport")

package com.takaotech.ktravel.data.archive.crypto

import at.asitplus.signum.indispensable.kdf.SCrypt
import at.asitplus.signum.indispensable.misc.bytes
import at.asitplus.signum.indispensable.symmetric.SymmetricEncryptionAlgorithm
import at.asitplus.signum.indispensable.symmetric.authTag
import at.asitplus.signum.indispensable.symmetric.from
import at.asitplus.signum.indispensable.symmetric.keyFrom
import at.asitplus.signum.indispensable.symmetric.nonce
import at.asitplus.signum.indispensable.symmetric.sealedBox
import at.asitplus.signum.supreme.kdf.deriveKey
import at.asitplus.signum.supreme.symmetric.decrypt
import at.asitplus.signum.supreme.symmetric.encrypt
import com.takaotech.ktravel.domain.archive.TravelArchiveError
import com.takaotech.ktravel.domain.archive.TravelArchiveException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import org.kotlincrypto.random.CryptoRand
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Seals and opens the credentials carried by a `.ktravel` archive, under a password the user chooses.
 *
 * scrypt derives the key and AES-256-GCM encrypts with it. The choice is deliberate on both counts:
 * scrypt is memory-hard, so the memory cost per attempt is what blunts a GPU attack — and the
 * password is the only secret in the scheme, so that cost is the whole defence. AES-256 is the
 * symmetric component of CNSA 2.0, which is the answer to the post-quantum question here: a
 * 256-bit symmetric cipher already resists Grover, and the post-quantum KEMs solve a key-agreement
 * problem this flow does not have.
 *
 * This is the only file that touches signum's API. `supreme` is pre-1.0 and may break across minor
 * versions, so keeping the surface here means a future upgrade is a one-file change.
 */
internal object ArchiveSecretsCipher {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /**
     * scrypt at N=2^15, r=8, p=1: 32 MiB per attempt.
     *
     * signum implements scrypt in pure Kotlin rather than delegating to a native library, so these
     * were picked by measurement rather than from the OWASP table directly. On an ARM desktop:
     *
     * | configuration                | memory | time    |
     * |------------------------------|--------|---------|
     * | N=2^14, r=8, p=5 (OWASP)     | 16 MiB | 3.18 s  |
     * | N=2^17, r=8, p=1 (OWASP)     | 128 MiB| 5.12 s  |
     * | N=2^15, r=8, p=1 (this one)  | 32 MiB | 1.28 s  |
     *
     * The OWASP rows are equivalent to each other by total work, which is what makes them all this
     * slow here. Raising `p` multiplies the work but not the peak memory — scrypt's memory-hardness
     * comes from `N * r` — so dropping to p=1 and spending the budget on N instead doubles the
     * memory an attacker needs per guess while more than halving the wait for the user. That is
     * strictly better on both counts than the configuration it replaces.
     *
     * These values are written into every envelope, so tuning them again later leaves existing
     * archives readable.
     */
    private const val COST = 1 shl 15
    private const val BLOCK_SIZE = 8
    private const val PARALLELIZATION = 1

    private const val SALT_BYTES = 16
    private const val KEY_BITS = 256

    private val algorithm = SymmetricEncryptionAlgorithm.AES_256.GCM

    /** Encrypts [payload] under [password]. */
    suspend fun seal(payload: ArchiveSecretsPayload, password: String): ArchiveSecretsEnvelope {
        val salt = CryptoRand.Default.nextBytes(ByteArray(SALT_BYTES))
        val key = deriveKey(password, salt, COST, BLOCK_SIZE, PARALLELIZATION)

        val plaintext = json.encodeToString(ArchiveSecretsPayload.serializer(), payload)
            .encodeToByteArray()

        // The nonce is generated per call by signum and carried in the sealed box; it is never
        // reused, which is the one thing GCM cannot survive.
        val box = runCatchingCancellable {
            algorithm.keyFrom(key).getOrThrow().encrypt(plaintext).getOrThrow()
        }

        return ArchiveSecretsEnvelope(
            salt = Base64.encode(salt),
            cost = COST,
            blockSize = BLOCK_SIZE,
            parallelization = PARALLELIZATION,
            nonce = Base64.encode(box.nonce),
            ciphertext = Base64.encode(box.encryptedData),
            authTag = Base64.encode(box.authTag),
        )
    }

    /**
     * Decrypts [envelope] under [password].
     *
     * @throws TravelArchiveException with [TravelArchiveError.WrongPassword] when the tag does not
     *  verify, or [TravelArchiveError.UnsupportedSecretsScheme] for a scheme this build cannot read.
     */
    @Suppress("ThrowsCount") // Each throw is a distinct, typed reason the caller has to tell apart.
    suspend fun open(envelope: ArchiveSecretsEnvelope, password: String): ArchiveSecretsPayload {
        if (envelope.scheme != ArchiveSecretsEnvelope.SCHEME_SCRYPT_AES256GCM) {
            throw TravelArchiveException(
                TravelArchiveError.UnsupportedSecretsScheme(envelope.scheme),
            )
        }

        // The parameters come from the archive, not from the constants above: an archive written by
        // a build with different settings still opens.
        val key = deriveKey(
            password = password,
            salt = decodeBase64(envelope.salt),
            cost = envelope.cost,
            blockSize = envelope.blockSize,
            parallelization = envelope.parallelization,
        )

        val plaintext = try {
            algorithm.sealedBox
                .withNonce(decodeBase64(envelope.nonce))
                .from(decodeBase64(envelope.ciphertext), decodeBase64(envelope.authTag))
                .getOrThrow()
                .decrypt(algorithm.keyFrom(key).getOrThrow())
                .getOrThrow()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (@Suppress("TooGenericExceptionCaught", "SwallowedException") failure: Throwable) {
            // TODO
            // Add some logs

            // Anything failing past this point means the tag did not verify. The archive was already
            // structurally validated, so the password is the variable. The original exception is
            // deliberately dropped: it describes the cipher's internals and would only tell an
            // attacker how far their guess got.
            throw TravelArchiveException(TravelArchiveError.WrongPassword)
        }

        return runCatchingCancellable {
            json.decodeFromString(ArchiveSecretsPayload.serializer(), plaintext.decodeToString())
        }
    }

    private suspend fun deriveKey(
        password: String,
        salt: ByteArray,
        cost: Int,
        blockSize: Int,
        parallelization: Int,
    ): ByteArray = runCatchingCancellable {
        SCrypt(cost = cost, parallelization = parallelization, blockSize = blockSize)
            .deriveKey(
                salt = salt,
                ikm = password.encodeToByteArray(),
                derivedKeyLength = (KEY_BITS / Byte.SIZE_BITS).bytes,
            )
            .getOrThrow()
    }

    private fun decodeBase64(value: String): ByteArray = runCatching { Base64.decode(value) }
        .getOrElse {
            throw TravelArchiveException(
                TravelArchiveError.CorruptedArchive("secrets entry is not valid base64"),
            )
        }

    /** Wraps anything unexpected as an archive error, while letting cancellation through. */
    private inline fun <T> runCatchingCancellable(block: () -> T): T = try {
        block()
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (exception: TravelArchiveException) {
        throw exception
    } catch (@Suppress("TooGenericExceptionCaught") failure: Throwable) {
        throw TravelArchiveException(
            TravelArchiveError.Io(failure.message ?: failure::class.simpleName.orEmpty()),
        )
    }
}
