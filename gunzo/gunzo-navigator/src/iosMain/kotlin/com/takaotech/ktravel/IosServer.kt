package com.takaotech.ktravel

/**
 * Entry point of the GunzoNavigator framework.
 *
 * Binds a port the operating system picks and reports it back, so the host never has to reserve one.
 *
 * Does not block: the caller keeps its run loop and stops the returned server itself. Note that iOS
 * suspends open sockets once the app leaves the foreground.
 */
suspend fun startGunzoNavigator(): RunningServer = startServerOnFreePort()
