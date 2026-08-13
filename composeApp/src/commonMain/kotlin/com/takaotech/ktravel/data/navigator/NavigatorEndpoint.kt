package com.takaotech.ktravel.data.navigator

/**
 * Where the app looks for gunzo-navigator.
 *
 * The two are interchangeable on purpose, and the app cannot tell them apart beyond this choice: the
 * contract, the transport and the code path are the same either way. That is what makes it possible
 * to move routing off the device later — or back onto it — without touching anything that computes
 * or draws a route.
 */
sealed interface NavigatorEndpoint {
    /**
     * The server runs inside this process, on a port the operating system assigns.
     *
     * The default, and the only one that works with no network at all.
     */
    data object Embedded : NavigatorEndpoint

    /**
     * The server runs somewhere else.
     *
     * @property baseUrl Its origin, such as `https://navigator.example.com`.
     */
    data class Remote(val baseUrl: String) : NavigatorEndpoint
}
