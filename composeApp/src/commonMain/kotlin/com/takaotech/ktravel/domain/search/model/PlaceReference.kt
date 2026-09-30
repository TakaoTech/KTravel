package com.takaotech.ktravel.domain.search.model

/**
 * How one provider knows a place: the only thing its details can be asked by.
 *
 * The identifier means nothing to another provider, which is why the provider travels with it: a
 * place found through HERE is looked up through HERE, whichever provider the search bar has moved
 * on to since.
 *
 * @property providerId The provider's identifier in the navigator contract, such as `here`.
 * @property placeId The provider's own identifier of the place, opaque and passed back unchanged.
 */
data class PlaceReference(val providerId: String, val placeId: String)
