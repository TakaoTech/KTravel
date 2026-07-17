package com.takaotech.ktravel.data.datasource

/**
 * Deduce un MIME type dall'estensione del file (case-insensitive). FileKit non espone il MIME,
 * quindi si mappano le estensioni comuni; il fallback è `application/octet-stream`.
 */
internal fun mimeTypeFromExtension(extension: String): String = when (extension.lowercase()) {
    "jpg", "jpeg" -> "image/jpeg"
    "png" -> "image/png"
    "gif" -> "image/gif"
    "webp" -> "image/webp"
    "bmp" -> "image/bmp"
    "heic", "heif" -> "image/heic"
    "svg" -> "image/svg+xml"
    "pdf" -> "application/pdf"
    "txt", "md" -> "text/plain"
    "csv" -> "text/csv"
    "json" -> "application/json"
    "doc" -> "application/msword"
    "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    "xls" -> "application/vnd.ms-excel"
    "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    "ppt" -> "application/vnd.ms-powerpoint"
    "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
    "zip" -> "application/zip"
    "mp4" -> "video/mp4"
    "mov" -> "video/quicktime"
    "mp3" -> "audio/mpeg"
    else -> "application/octet-stream"
}
