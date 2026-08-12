package com.takaotech.ktravel

import io.ktor.server.cio.EngineMain

/**
 * JVM entry point. The modules to install are resolved by name from `application.conf`, which needs
 * reflection and therefore only works here: Android and iOS call `startServer` instead.
 */
fun main(args: Array<String>) {
    EngineMain.main(args)
}
