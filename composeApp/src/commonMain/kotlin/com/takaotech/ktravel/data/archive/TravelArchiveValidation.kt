package com.takaotech.ktravel.data.archive

/**
 * Validazione dei path che arrivano dall'archivio.
 *
 * `travel.json` è **input non fidato**: un `relative_path` come `../../../evil` farebbe scrivere
 * l'estrazione fuori dalla root degli allegati (zip-slip). Ogni path va validato prima di
 * qualunque scrittura su disco.
 */
internal object TravelArchiveValidation {

    private val SEGMENT = Regex("[A-Za-z0-9._-]+")
    private const val EXPECTED_SEGMENTS = 3

    /** True se [path] è nella forma sicura `<travelId>/<stepId>/<fileName>`. */
    fun isSafeRelativePath(path: String): Boolean {
        if (path.isEmpty() || path.startsWith('/') || path.contains('\\')) return false
        val segments = path.split('/')
        if (segments.size != EXPECTED_SEGMENTS) return false
        return segments.all { segment ->
            segment != "." && segment != ".." && SEGMENT.matches(segment)
        }
    }
}
