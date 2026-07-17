package com.takaotech.ktravel.ui.planning.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Editor per le note in Markdown, pilotato da un [MarkdownEditorController] condiviso.
 *
 * Su Android/Desktop usa l'editor WYSIWYG Hyphen con una toolbar di formattazione; su iOS (dove
 * Hyphen non è disponibile) usa un fallback con un campo di testo che modifica il Markdown grezzo.
 * Il Markdown corrente si osserva via [MarkdownEditorController.markdownFlow]; l'inizializzazione
 * avviene alla creazione del controller ([rememberMarkdownEditorController]).
 */
@Composable
expect fun MarkdownNoteEditor(
    controller: MarkdownEditorController,
    label: String,
    modifier: Modifier = Modifier,
)
