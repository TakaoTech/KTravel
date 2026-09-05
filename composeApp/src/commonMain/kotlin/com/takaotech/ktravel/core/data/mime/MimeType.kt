package com.takaotech.ktravel.core.data.mime

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.jvm.JvmInline

/**
 * IANA media type of a file, such as `image/jpeg`. [MimeTypes.fromExtension] derives one from a
 * file extension; the catalog of known types lives in [MimeTypes].
 *
 * On the wire it is a plain string, so it is stored and read exactly as any other media type ever
 * written into an archive, including one this build does not know about.
 */
@Serializable(with = MimeTypeSerializer::class)
@JvmInline
value class MimeType(
    /** The media type itself, e.g. `image/jpeg`. */
    val value: String,
) {

    /** Whether the type belongs to the `image/` tree, the one the UI can preview inline. */
    val isImage: Boolean get() = value.startsWith("image/", ignoreCase = true)

    override fun toString(): String = value
}

/** Reads and writes a [MimeType] as its plain string form, e.g. `"mime_type": "image/jpeg"`. */
object MimeTypeSerializer : KSerializer<MimeType> {
    override val descriptor = PrimitiveSerialDescriptor("MimeType", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): MimeType = MimeType(decoder.decodeString())

    override fun serialize(encoder: Encoder, value: MimeType) {
        encoder.encodeString(value.value)
    }
}
