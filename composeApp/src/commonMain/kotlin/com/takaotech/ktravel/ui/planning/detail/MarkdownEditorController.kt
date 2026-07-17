package com.takaotech.ktravel.ui.planning.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.Flow

/**
 * Handle condiviso sull'editor Markdown, così che azioni esterne (es. la sezione inventario) possano
 * inserire un riferimento **alla posizione corrente del cursore** senza conoscere l'implementazione
 * dell'editor (Hyphen su Android/Desktop, `TextFieldValue` su iOS).
 */
@Stable
expect class MarkdownEditorController {
    /** Flusso del Markdown corrente; emette a ogni modifica. */
    val markdownFlow: Flow<String>

    /** Inserisce [text] alla posizione corrente del cursore. */
    fun insertAtCursor(text: String)
}

/** Crea e ricorda un [MarkdownEditorController] inizializzato con [initialValue]. */
@Composable
expect fun rememberMarkdownEditorController(initialValue: String): MarkdownEditorController
