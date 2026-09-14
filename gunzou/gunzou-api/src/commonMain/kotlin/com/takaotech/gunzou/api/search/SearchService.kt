package com.takaotech.gunzou.api.search

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * Identifies one search service, that is the last segment of a `/{provider}/search/{service}` path:
 * autocomplete is one, a future lookup or a browse by category would be others.
 *
 * A string and not an enum for the same forward compatibility reason as
 * [com.takaotech.gunzou.api.common.ProviderId]: a server that gains a service must not break the
 * catalog decoding of a client built before it existed.
 *
 * @property value The service name as it travels on the wire.
 */
@Serializable
@JvmInline
value class SearchService(val value: String) {
    override fun toString(): String = value

    /** The services this contract currently serves. */
    companion object {
        /** Suggestions for the text a user is typing, one keystroke at a time. */
        val AUTOCOMPLETE = SearchService("autocomplete")
    }
}
