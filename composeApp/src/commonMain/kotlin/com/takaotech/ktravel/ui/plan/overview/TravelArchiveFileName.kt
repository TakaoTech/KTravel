package com.takaotech.ktravel.ui.plan.overview

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

/**
 * File name proposed by the saver: the trip name is free text, and on some platforms a path
 * separator would make it unusable.
 */
internal fun String.toArchiveFileName(): String = replace(Regex("""[^\p{L}\p{N} _-]"""), "").trim().ifEmpty { "travel" }
