package com.takaotech.ktravel.core.logging

import co.touchlab.kermit.Logger

/**
 * Points whatever logging framework the platform has at the application's logger.
 *
 * On the JVM and Android that means SLF4J: Ktor, Couchbase and the coroutines integration all write
 * through it, and this application is their backend (see `core/logging/slf4j`). On iOS there is no
 * such framework and nothing to point anywhere.
 *
 * @param logger The one logger of the process.
 */
expect fun installPlatformLogBridge(logger: Logger)
