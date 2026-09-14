package com.takaotech.gunzou.here.search.autocomplete.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response of the Autocomplete endpoint.
 *
 * @property items Completions, most likely first
 */
@Serializable
data class AutocompleteResponse(@SerialName("items") val items: List<AutocompleteResultItem>)
