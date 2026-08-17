package com.takaotech.ktravel

import co.touchlab.kermit.Logger
import io.ktor.server.application.Application
import org.koin.core.module.Module

/**
 * Everything the server installs on every target.
 *
 * The JVM adds compression, metrics, OpenAPI and Swagger on top of this through
 * `Application.jvmModule`: those Ktor modules publish no native variant.
 *
 * Order matters in one place: [configureStatusPages] must be installed before the routes, so the
 * plugin is wrapping them when one throws.
 *
 * Called with no argument this is the embedded server — no key of its own, no limit. A deployment
 * passes a [NavigatorServerConfig] instead.
 */
fun Application.module(): Unit = module(NavigatorServerConfig.EMBEDDED, koinOverrides = null, logger = null)

/**
 * The same, configured for a deployment.
 *
 * @param config What this instance requires of its callers and what it can do for them.
 */
fun Application.module(config: NavigatorServerConfig): Unit = module(config, koinOverrides = null, logger = null)

/**
 * The embedded configuration, with part of its wiring replaced. Only tests use this.
 */
internal fun Application.module(koinOverrides: Module?): Unit =
    module(NavigatorServerConfig.EMBEDDED, koinOverrides, logger = null)

/**
 * The same, with part of its wiring replaced.
 *
 * Internal, and without default values, so [Module] never appears in a signature a consumer has to
 * resolve: koin-ktor is an implementation dependency of this module.
 *
 * @param koinOverrides Definitions replacing the real ones. Only tests pass this.
 * @param logger The host's logger, when this server runs inside an application rather than as one.
 *   Null means there is no host and the server configures logging itself. See [installLogging].
 */
internal fun Application.module(config: NavigatorServerConfig, koinOverrides: Module?, logger: Logger? = null) {
    installLogging(logger)
    configureKoin(koinOverrides)
    configureSerialization()
    configureStatusPages()
    configureSecurity(config)
    configureRequestValidation()
    configureCaching()
    configureRouting(config)
}
