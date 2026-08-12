package com.takaotech.ktravel

import io.ktor.server.cio.CIOApplicationEngine
import io.ktor.server.engine.EmbeddedServer

/**
 * Entry point of the GunzoNavigator framework.
 *
 * Does not block: the caller keeps its run loop and stops the returned server itself. Note that iOS
 * suspends open sockets once the app leaves the foreground.
 */
fun startGunzoNavigator(
    port: Int = DEFAULT_PORT,
): EmbeddedServer<CIOApplicationEngine, CIOApplicationEngine.Configuration> = startServer(port = port, wait = false)
