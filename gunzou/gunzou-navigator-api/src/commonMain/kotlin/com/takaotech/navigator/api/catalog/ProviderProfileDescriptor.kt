package com.takaotech.navigator.api.catalog

import com.takaotech.navigator.api.common.ProviderId
import com.takaotech.navigator.api.common.ProviderProfile
import com.takaotech.navigator.api.common.TravelMode
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
 * @property path Where to POST a request for it, such as `/v1/here/car`.
 * @property displayName A name to put in a selector. English, like everything else in the contract:
 *   translating it is the client's job, since only the client knows the user's language.
 * @property supportedModes The modes this profile can route for.
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
    val supportedModes: List<TravelMode> = emptyList(),
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
@Serializable
data class ProviderCatalogResponse(val profiles: List<ProviderProfileDescriptor> = emptyList())
