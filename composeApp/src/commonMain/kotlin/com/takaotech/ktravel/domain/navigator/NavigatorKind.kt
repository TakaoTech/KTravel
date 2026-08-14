package com.takaotech.ktravel.domain.navigator

/**
 * Which navigator computes a route.
 *
 * The same server answers in both cases and the contract between them is identical — the remote one
 * is not a richer service, it is the same build with a configuration. What differs is only what can
 * go wrong: the embedded one cannot be unreachable, and the remote one cannot be restarted.
 */
enum class NavigatorKind {
    /** The server running inside this process, on a port the operating system assigns. */
    EMBEDDED,

    /** A deployment reached over the network, at the address the settings hold. */
    REMOTE,

    ;

    companion object {
        /**
         * The choice a stored preference names, falling back to [EMBEDDED] for anything unknown.
         *
         * Stored as a string rather than as a serialized enum precisely so this can be lenient: a
         * document written by a newer build naming a kind this one has never heard of has to remain
         * readable, and the embedded server is the answer that always works.
         */
        fun ofOrEmbedded(stored: String): NavigatorKind =
            entries.firstOrNull { it.name.equals(stored, ignoreCase = true) } ?: EMBEDDED
    }
}
