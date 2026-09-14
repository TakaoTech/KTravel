package com.takaotech.gunzou.here.search.browser.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response of the Browse endpoint.
 *
 * The pagination fields are only present when the request set an offset.
 *
 * @property items Results, nearest first: the full list, or one page of it when paginating
 * @property offset Offset of this page in the full list, echoing the request
 * @property nextOffset Offset of the next page. Absent on the last page.
 * @property count Number of results in this page
 * @property limit Maximum number of results per page, echoing the request
 */
@Serializable
data class BrowseResponse(
    @SerialName("items") val items: List<BrowseResultItem>,
    @SerialName("offset") val offset: Int? = null,
    @SerialName("nextOffset") val nextOffset: Int? = null,
    @SerialName("count") val count: Int? = null,
    @SerialName("limit") val limit: Int? = null,
)
