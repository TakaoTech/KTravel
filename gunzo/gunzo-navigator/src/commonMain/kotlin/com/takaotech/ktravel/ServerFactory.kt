package com.takaotech.ktravel

import io.ktor.server.cio.CIO
import io.ktor.server.cio.CIOApplicationEngine
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer

const val DEFAULT_PORT: Int = 8080

/**
 * Starts the server on every target.
 *
 * The JVM entry point goes through `EngineMain` and `application.conf` instead, which resolves its
 * modules by name through reflection and therefore cannot work on Android or iOS.
 *
 * @param wait blocks the calling thread until the server stops. Pass `false` when the caller owns
 * the run loop, as an iOS or Android host does.
 */
fun startServer(
    port: Int = DEFAULT_PORT,
    wait: Boolean = true,
): EmbeddedServer<CIOApplicationEngine, CIOApplicationEngine.Configuration> =
    embeddedServer(CIO, port = port) { module() }.apply { start(wait) }
