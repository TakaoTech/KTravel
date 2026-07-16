package com.takaotech.ktravel.ui.planning.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Editor per le note in Markdown.
 *
 * Su Android/Desktop usa l'editor WYSIWYG Hyphen con una toolbar di formattazione; su iOS (dove
 * Hyphen non è disponibile) usa un fallback con un campo di testo che modifica il Markdown grezzo.
 * [onValueChange] emette il Markdown corrente a ogni modifica; [initialValue] è usato solo per
 * l'inizializzazione.
 */
@Composable
expect fun MarkdownNoteEditor(
    initialValue: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
)
