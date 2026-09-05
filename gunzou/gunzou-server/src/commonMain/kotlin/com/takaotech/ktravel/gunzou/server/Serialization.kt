package com.takaotech.ktravel.gunzou.server

import com.takaotech.gunzou.api.NavigatorJson
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation

/**
 * Reads and writes the wire contract.
 *
 * The [NavigatorJson] instance is the contract's own, shared with every client: configuring a second
 * one here with slightly different settings is exactly how a server starts emitting payloads its
 * clients cannot read.
 */
fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(NavigatorJson)
    }
}
