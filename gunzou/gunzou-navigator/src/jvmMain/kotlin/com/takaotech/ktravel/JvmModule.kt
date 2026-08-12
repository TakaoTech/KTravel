package com.takaotech.ktravel

import io.ktor.server.application.Application

/**
 * The single module `application.conf` refers to by name. Reflection based module loading only works
 * on the JVM, so Android and iOS call [startServer], which installs [module] directly.
 */
fun Application.jvmModule() {
    module()
    configureHttpJvm()
    configureMonitoring()
}
