package com.takaotech.ktravel.ui.shared.format

import androidx.compose.ui.graphics.Color

/** A colour is `#RRGGBB`: a hash and six hex digits, nothing longer and nothing shorter. */
private const val HEX_COLOUR_LENGTH = 7

/** Feeds publish colours without an alpha channel, so every one of them is opaque. */
private const val OPAQUE_ALPHA = 0xFF000000L

/**
 * The `#RRGGBB` a transport agency publishes, when it is one this can draw.
 *
 * Only that exact form is accepted. A feed publishing anything else falls back to the theme rather
 * than to a colour guessed from half a string, which is how a line ends up drawn in black on black.
 */
internal fun String.toColorOrNull(): Color? = takeIf { it.length == HEX_COLOUR_LENGTH && it.startsWith('#') }
    ?.substring(1)
    ?.toLongOrNull(radix = 16)
    ?.let { Color(it or OPAQUE_ALPHA) }
