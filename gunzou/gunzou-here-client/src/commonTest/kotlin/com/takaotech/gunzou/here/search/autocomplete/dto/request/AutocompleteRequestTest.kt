package com.takaotech.gunzou.here.search.autocomplete.dto.request

import com.takaotech.gunzou.here.common.model.Coordinate
import com.takaotech.gunzou.here.search.common.model.SearchArea
import com.vanniktech.locale.Locale
import kotlin.test.Test
import kotlin.test.assertFailsWith

class AutocompleteRequestTest {

    private val lang = listOf(Locale.from("en-US"))
    private val milan = Coordinate(45.4642, 9.19)

    @Test
    fun `Given a search centre When countries are also set Then the request is accepted`() {
        AutocompleteRequest(query = "Via", at = milan, area = SearchArea.Countries(listOf("ITA")), lang = lang)
    }

    @Test
    fun `Given a search centre When a circle or a bounding box is also set Then the request is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            AutocompleteRequest(query = "Via", at = milan, area = SearchArea.Circle(milan, 1_000), lang = lang)
        }
        assertFailsWith<IllegalArgumentException> {
            AutocompleteRequest(
                query = "Via",
                at = milan,
                area = SearchArea.BoundingBox(9.0, 45.0, 9.5, 45.6),
                lang = lang,
            )
        }
    }

    @Test
    fun `Given invalid values When the request is built Then it is rejected`() {
        assertFailsWith<IllegalArgumentException> { AutocompleteRequest(query = "  ", lang = lang) }
        assertFailsWith<IllegalArgumentException> { AutocompleteRequest(query = "Via", lang = emptyList()) }
        assertFailsWith<IllegalArgumentException> { AutocompleteRequest(query = "Via", lang = lang, limit = 0) }
        assertFailsWith<IllegalArgumentException> { AutocompleteRequest(query = "Via", lang = lang, limit = 21) }
    }
}
