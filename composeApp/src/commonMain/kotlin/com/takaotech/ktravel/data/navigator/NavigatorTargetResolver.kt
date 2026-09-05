package com.takaotech.ktravel.data.navigator

import com.takaotech.gunzou.client.NavigatorTarget
import com.takaotech.ktravel.di.PlanningGraphScope
import com.takaotech.ktravel.domain.navigator.NavigatorKind
import com.takaotech.ktravel.domain.repository.AppSettingsRepository
import com.takaotech.ktravel.domain.repository.SettingsRepository
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

/**
 * Turns "embedded or remote" into an address to call.
 *
 * Three things have a say, and they are deliberately not the same kind of thing. The installation
 * holds where the remote navigator is, because that outlives any trip. The plan may override it,
 * because a trip abroad can be worth computing somewhere else. The screen decides which of the two
 * is used right now, and does not write that decision back.
 *
 * Lives in [PlanningGraphScope] because two of the three inputs are the plan's, and it is resolved on
 * every call rather than cached: an address edited in settings has to take effect on the next route,
 * not on the next launch.
 */
@SingleIn(PlanningGraphScope::class)
@Inject
class NavigatorTargetResolver(
    private val appSettings: AppSettingsRepository,
    private val planSettings: SettingsRepository,
    private val embeddedHost: EmbeddedNavigatorHost,
) {

    /** Which navigator the transport screen should open on for this plan. */
    fun defaultKind(): NavigatorKind = planSettings.settings.navigatorPreference

    /**
     * Whether the remote option can be offered at all.
     *
     * Without an address there is nothing to call, and offering the choice would produce a failure
     * the traveller cannot act on from the screen they are looking at.
     */
    fun isRemoteConfigured(): Boolean = remoteTarget() != null

    /**
     * Where a call of this kind goes.
     *
     * @throws IllegalStateException asking for [NavigatorKind.REMOTE] with no address configured,
     *   which the caller is expected to have ruled out with [isRemoteConfigured].
     */
    suspend fun resolve(kind: NavigatorKind): NavigatorTarget = when (kind) {
        NavigatorKind.EMBEDDED -> NavigatorTarget(baseUrl = embeddedHost.baseUrl())
        NavigatorKind.REMOTE -> checkNotNull(remoteTarget()) { "No remote navigator is configured" }
    }

    /**
     * The same, after the previous attempt failed to reach anything.
     *
     * Only the embedded server has a recovery: its socket does not survive the app being suspended,
     * so it is restarted and comes back on a new port. A remote one that did not answer will not
     * answer differently for being asked twice in a row, so the address is simply returned unchanged
     * and the caller is expected not to retry it.
     */
    suspend fun recover(kind: NavigatorKind): NavigatorTarget = when (kind) {
        NavigatorKind.EMBEDDED -> NavigatorTarget(baseUrl = embeddedHost.restart())
        NavigatorKind.REMOTE -> resolve(kind)
    }

    /**
     * The remote deployment this plan should use, or null when none is configured anywhere.
     *
     * No credential travels with the address. The only one a navigator is given is the trip's own
     * provider key, and that is attached to the routing call rather than to the destination — the
     * navigator itself is reached anonymously.
     */
    private fun remoteTarget(): NavigatorTarget? {
        val plan = planSettings.settings
        if (plan.overridesNavigatorRemote) return NavigatorTarget(baseUrl = plan.navigatorRemoteBaseUrl)

        val app = appSettings.settings.value

        return if (app.hasRemoteNavigator) NavigatorTarget(baseUrl = app.navigatorRemoteBaseUrl) else null
    }
}
