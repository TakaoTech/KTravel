package com.takaotech.ktravel.core

/**
 * Informazioni di build accessibili da `commonMain`.
 *
 * Il valore è puramente diagnostico: gli archivi non vengono mai accettati o rifiutati in base ad
 * esso, il gating è sempre e solo su `schema_version`.
 */

//TODO Replace with BuildKonfig
object KTravelBuildInfo {
    const val VERSION: String = "1.0.0"
}
