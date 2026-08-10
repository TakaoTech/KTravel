package com.takaotech.ktravel.data.archive.crypto

import com.takaotech.ktravel.domain.archive.TravelArchiveError
import com.takaotech.ktravel.domain.archive.TravelArchiveException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotContain

class ArchiveSecretsCipherTest :
    BehaviorSpec({

        val apiKey = "HERE-abcdef0123456789"
        val password = "correct horse battery staple"

        given("a payload sealed under a password") {
            `when`("it is opened with the same password") {
                val envelope = ArchiveSecretsCipher.seal(ArchiveSecretsPayload(apiKey), password)
                val payload = ArchiveSecretsCipher.open(envelope, password)

                then("the original key comes back") {
                    payload.hereApiKey shouldBe apiKey
                }
                then("the envelope declares the scheme it was sealed with") {
                    envelope.scheme shouldBe ArchiveSecretsEnvelope.SCHEME_SCRYPT_AES256GCM
                }
                then("the envelope carries the KDF parameters, so it stays readable if they change") {
                    envelope.cost shouldBe (1 shl 15)
                    envelope.blockSize shouldBe 8
                    envelope.parallelization shouldBe 1
                }
                then("the key never appears in the ciphertext") {
                    envelope.ciphertext shouldNotContain apiKey
                    envelope.salt shouldNotContain apiKey
                }
            }

            `when`("it is opened with the wrong password") {
                val envelope = ArchiveSecretsCipher.seal(ArchiveSecretsPayload(apiKey), password)

                then("it fails as a wrong password rather than as a corrupted archive") {
                    val failure = shouldThrow<TravelArchiveException> {
                        ArchiveSecretsCipher.open(envelope, "not the password")
                    }
                    failure.error shouldBe TravelArchiveError.WrongPassword
                }
            }

            `when`("the ciphertext is tampered with") {
                val envelope = ArchiveSecretsCipher.seal(ArchiveSecretsPayload(apiKey), password)
                val tampered = envelope.copy(
                    ciphertext = envelope.ciphertext.replaceFirstChar { if (it == 'A') 'B' else 'A' },
                )

                then("the authentication tag catches it") {
                    val failure = shouldThrow<TravelArchiveException> {
                        ArchiveSecretsCipher.open(tampered, password)
                    }
                    failure.error shouldBe TravelArchiveError.WrongPassword
                }
            }
        }

        given("two seals of the same payload under the same password") {
            `when`("they are compared") {
                val first = ArchiveSecretsCipher.seal(ArchiveSecretsPayload(apiKey), password)
                val second = ArchiveSecretsCipher.seal(ArchiveSecretsPayload(apiKey), password)

                then("the salts differ, so the same password never derives the same key twice") {
                    first.salt shouldNotBe second.salt
                }
                then("the nonces differ, which is what GCM cannot survive without") {
                    first.nonce shouldNotBe second.nonce
                }
                then("the ciphertexts differ") {
                    first.ciphertext shouldNotBe second.ciphertext
                }
            }
        }

        given("an archive sealed by a build using different KDF parameters") {
            `when`("it is opened by this build") {
                // Hand-built with the parameters this build no longer uses, to prove the envelope — not
                // the constants in the cipher — is what drives derivation. This is what lets the
                // parameters be retuned without stranding archives already in the wild.
                val envelope = ArchiveSecretsEnvelope(
                    salt = "AAAAAAAAAAAAAAAAAAAAAA==",
                    cost = 1 shl 12,
                    blockSize = 8,
                    parallelization = 1,
                    nonce = "AAAAAAAAAAAAAAAA",
                    ciphertext = "AAAAAAAAAAAAAAAAAAAAAA==",
                    authTag = "AAAAAAAAAAAAAAAAAAAAAA==",
                )

                then("the old parameters are honoured, so it fails on the password and not on the format") {
                    val failure = shouldThrow<TravelArchiveException> {
                        ArchiveSecretsCipher.open(envelope, password)
                    }
                    failure.error shouldBe TravelArchiveError.WrongPassword
                }
            }
        }

        given("an envelope sealed with an unknown scheme") {
            `when`("it is opened") {
                val envelope = ArchiveSecretsCipher.seal(ArchiveSecretsPayload(apiKey), password)
                    .copy(scheme = "mlkem768-aes256gcm-v1")

                then("it is rejected by name instead of being attacked with the wrong algorithm") {
                    val failure = shouldThrow<TravelArchiveException> {
                        ArchiveSecretsCipher.open(envelope, password)
                    }
                    failure.error shouldBe
                        TravelArchiveError.UnsupportedSecretsScheme("mlkem768-aes256gcm-v1")
                }
            }
        }

        given("an envelope whose base64 is not decodable") {
            `when`("it is opened") {
                val envelope = ArchiveSecretsCipher.seal(ArchiveSecretsPayload(apiKey), password)
                    .copy(salt = "this is not base64!!")

                then("it is reported as a corrupted archive") {
                    val failure = shouldThrow<TravelArchiveException> {
                        ArchiveSecretsCipher.open(envelope, password)
                    }
                    failure.error shouldBe
                        TravelArchiveError.CorruptedArchive("secrets entry is not valid base64")
                }
            }
        }

        given("an empty api key") {
            `when`("it is sealed and opened") {
                val envelope = ArchiveSecretsCipher.seal(ArchiveSecretsPayload(""), password)

                then("it round trips without special casing") {
                    ArchiveSecretsCipher.open(envelope, password).hereApiKey shouldBe ""
                }
            }
        }

        given("a password with characters outside ASCII") {
            `when`("it is used to seal and open") {
                val unicodePassword = "città-日本-🔐"
                val envelope = ArchiveSecretsCipher.seal(ArchiveSecretsPayload(apiKey), unicodePassword)

                then("it round trips, so the password is encoded consistently") {
                    ArchiveSecretsCipher.open(envelope, unicodePassword).hereApiKey shouldBe apiKey
                }
            }
        }
    })
