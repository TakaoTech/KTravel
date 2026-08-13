package com.takaotech.ktravel.data.navigator

import com.takaotech.ktravel.di.AppScope
import com.takaotech.navigator.client.NavigatorBaseUrl
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

/**
 * Turns the configured [NavigatorEndpoint] into an origin, on every request.
 *
 * This is the whole of "embedded and remote are interchangeable". Everything above it — the routing
 * provider, the screens — asks a [com.takaotech.navigator.client.NavigatorClient] for a route and
 * never learns which of the two answered. Switching is changing what [endpoint] returns.
 *
 * Resolved per request and never cached: an embedded server binds a new port every time it is
 * restarted, and a remote base URL can be edited in settings while the app is running.
 *
 * @param endpoint Which navigator to use. A function rather than a value because the choice is a
 *   setting the user can change without the app being restarted.
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class NavigatorBaseUrlResolver(
    private val embeddedHost: EmbeddedNavigatorHost,
    private val endpoint: () -> NavigatorEndpoint,
) : NavigatorBaseUrl {

    override suspend fun resolve(): String = when (val current = endpoint()) {
        NavigatorEndpoint.Embedded -> embeddedHost.baseUrl()
        is NavigatorEndpoint.Remote -> current.baseUrl.trimEnd('/')
    }

    /**
     * Starts the embedded server again and returns its new origin.
     *
     * Only meaningful for [NavigatorEndpoint.Embedded]; a remote navigator being unreachable is not
     * something this app can fix, so the same URL is returned and the caller's retry will fail the
     * same way rather than pretending otherwise.
     */
    suspend fun recover(): String = when (val current = endpoint()) {
        NavigatorEndpoint.Embedded -> embeddedHost.restart()
        is NavigatorEndpoint.Remote -> current.baseUrl.trimEnd('/')
    }
}
