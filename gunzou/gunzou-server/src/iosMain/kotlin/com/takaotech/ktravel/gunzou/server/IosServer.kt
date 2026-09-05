package com.takaotech.ktravel.gunzou.server

import co.touchlab.kermit.Logger

/**
 * Entry point of the GunzoNavigator framework.
 *
 * Binds a port the operating system picks and reports it back, so the host never has to reserve one.
 *
 * Does not block: the caller keeps its run loop and stops the returned server itself. Note that iOS
 * suspends open sockets once the app leaves the foreground.
 *
 * @param logger The host's logger, so the server's lines land wherever the host sends its own. Null
 *   lets the server configure logging itself.
 */
suspend fun startGunzouNavigator(logger: Logger? = null): RunningServer = startServerOnFreePort(logger)
