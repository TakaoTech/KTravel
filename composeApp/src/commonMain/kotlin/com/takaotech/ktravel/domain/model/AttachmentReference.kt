package com.takaotech.ktravel.domain.model

/**
 * Schema custom per referenziare un file dell'inventario dentro il Markdown della nota.
 *
 * Il custom scheme `ktravel://attachment/<relativePath>` distingue in modo affidabile i nostri
 * allegati dai link/immagini esterni, sia nel transformer immagini sia nell'handler dei link.
 */
object AttachmentReference {

    const val URI_PREFIX: String = "ktravel://attachment/"

    private val REGEX = Regex(Regex.escape(URI_PREFIX) + "([^)\\s]+)")

    /** Snippet Markdown per un'immagine inline. */
    fun imageMarkdown(relativePath: String, altText: String = ""): String =
        "![$altText]($URI_PREFIX$relativePath)"

    /** Snippet Markdown per un link a file (apribile nativamente). */
    fun fileMarkdown(relativePath: String, label: String): String =
        "[$label]($URI_PREFIX$relativePath)"

    /** True se l'URI è un riferimento a un allegato dell'inventario. */
    fun isAttachmentUri(uri: String): Boolean = uri.startsWith(URI_PREFIX)

    /** Estrae il path relativo da un URI `ktravel://attachment/<rel>`, o null se non lo è. */
    fun relativePathOf(uri: String): String? =
        if (isAttachmentUri(uri)) uri.removePrefix(URI_PREFIX) else null

    /** Tutti i path relativi referenziati (immagini e link) nel [markdown]. */
    fun extractRelativePaths(markdown: String): List<String> =
        REGEX.findAll(markdown).map { it.groupValues[1] }.toList()

    /**
     * Path relativi referenziati nel [markdown] ma **non presenti** tra gli [inventoryRelativePaths]
     * dell'inventario: riferimenti "dangling" da segnalare come errore.
     */
    fun missingReferences(
        markdown: String,
        inventoryRelativePaths: Collection<String>
    ): List<String> {
        val inventory = inventoryRelativePaths.toSet()
        return extractRelativePaths(markdown).filter { it !in inventory }.distinct()
    }
}
