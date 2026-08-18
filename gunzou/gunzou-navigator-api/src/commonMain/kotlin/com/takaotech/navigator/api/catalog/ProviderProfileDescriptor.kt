package com.takaotech.navigator.api.catalog

import com.takaotech.navigator.api.common.ProviderId
import com.takaotech.navigator.api.common.ProviderProfile
import kotlinx.serialization.Serializable

/**
 * What one profile of one provider can do.
 *
 * This is what makes the server worth having in front of the engines. Without it the app would carry
 * a `when` over the providers to decide which controls to draw and which limits to enforce, and that
 * `when` would have to be updated, and shipped, every time the server gained a profile. With it the
 * app draws its selector from `GET /v1/profiles` and a new engine reaches users without an app
 * release.
 *
 * @property provider The engine.
 * @property profile Which of its APIs this describes.
 * @property path Where to POST a request for it, such as `/v1/here/transit`. A profile whose path
 *   names something the caller chooses publishes the template — `/v1/here/routing/{transportMode}`
 *   for the road profile — and the caller fills it in from [supportedModes].
 * @property displayName A name to put in a selector. English, like everything else in the contract:
 *   translating it is the client's job, since only the client knows the user's language.
 * @property supportedModes The modes this profile accepts, spelled in the vocabulary of the API that
 *   answers it — see [SupportedModes]. It is not a flat list precisely because two profiles of the
 *   same provider do not share one: what the road endpoint calls a `bus` and what the transit one
 *   calls a `bus` are different vehicles, and merging them would offer a caller a mode the endpoint
 *   would reject.
 * @property maxAlternatives The largest `alternatives` it accepts.
 * @property maxVia The most intermediate stops it accepts. Zero means the profile takes none.
 * @property supportsArriveBy Whether [com.takaotech.navigator.api.common.RouteTime.ArriveBy] works.
 * @property supportsTolls Whether it can price the tolls along a route.
 * @property requiresApiKey Whether a call without a provider key will be rejected.
 */
@Serializable
data class ProviderProfileDescriptor(
    val provider: ProviderId,
    val profile: ProviderProfile,
    val path: String,
    val displayName: String,
    val supportedModes: SupportedModes = SupportedModes.None,
    val maxAlternatives: Int,
    val maxVia: Int = 0,
    val supportsArriveBy: Boolean = false,
    val supportsTolls: Boolean = false,
    val requiresApiKey: Boolean = false,
)

/**
 * Body of `GET /v1/profiles`: everything this server can route with, right now.
 *
 * @property profiles The available profiles. A profile whose provider is misconfigured is absent
 *   rather than present and broken, so a client can trust that what it lists will answer.
 */

/**
 * What one navigator serves.
 *
 * @property profiles The profiles this deployment actually mounts, which is only half of what a
 *   client needs: the other half is [NavigatorProfile.ALL], the profiles the contract can express.
 *   Subtracting one from the other is how a caller tells "this server does not serve it" from "this
 *   version cannot ask for it".
 * @property version The build answering, defaulted so that a response from a server older than this
 *   field still decodes. Carried here as well as on the health endpoint because loading the catalog
 *   is already the proof that a navigator is reachable, and a second call to learn its name would be
 *   a second round trip for something the first one could have said.
 */
@Serializable
data class ProviderCatalogResponse(
    val profiles: List<ProviderProfileDescriptor> = emptyList(),
    val version: String = "",
)
