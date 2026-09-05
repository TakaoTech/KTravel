package com.takaotech.ktravel.data.archive

import com.takaotech.ktravel.data.archive.TravelArchiveValidation.SEGMENT

/**
 * Validation of the paths coming out of an archive.
 *
 * `travel.json` is **untrusted input**: a `relative_path` such as `../../../evil` would make the
 * extraction write outside the attachment root (zip slip). Every path has to be validated before
 * any write to disk.
 */
internal object TravelArchiveValidation {

    private val SEGMENT = Regex("[A-Za-z0-9._-]+")
    private const val EXPECTED_SEGMENTS = 3

    /**
     * True when [path] has the safe `<travelId>/<stepId>/<fileName>` shape.
     *
     * The check is a whitelist rather than a blacklist of traversal sequences: the three segments
     * must match [SEGMENT], which admits no separator, no drive letter and no empty segment, so a
     * path that gets through can only resolve under the attachment root.
     */
    fun isSafeRelativePath(path: String): Boolean {
        if (path.isEmpty() || path.startsWith('/') || path.contains('\\')) return false
        val segments = path.split('/')
        if (segments.size != EXPECTED_SEGMENTS) return false
        return segments.all { segment ->
            segment != "." && segment != ".." && SEGMENT.matches(segment)
        }
    }
}
