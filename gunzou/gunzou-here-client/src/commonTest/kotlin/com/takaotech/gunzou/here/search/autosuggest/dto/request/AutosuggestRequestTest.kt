package com.takaotech.gunzou.here.search.autosuggest.dto.request

import com.takaotech.gunzou.here.common.model.Coordinate
import com.takaotech.gunzou.here.search.common.model.SearchArea
import com.vanniktech.locale.Locale
import kotlin.test.Test
import kotlin.test.assertFailsWith

class AutosuggestRequestTest {

    private val lang = listOf(Locale.from("en-US"))
    private val milan = Coordinate(45.4642, 9.19)

    @Test
    fun `Given a search centre or a locating area When the request is built Then it is accepted`() {
        AutosuggestRequest(query = "restau", at = milan, lang = lang)
        AutosuggestRequest(query = "restau", area = SearchArea.Circle(milan, 1_000), lang = lang)
        AutosuggestRequest(query = "restau", area = SearchArea.BoundingBox(9.0, 45.0, 9.5, 45.6), lang = lang)
        AutosuggestRequest(query = "restau", at = milan, area = SearchArea.Countries(listOf("ITA")), lang = lang)
    }

    @Test
    fun `Given no search context When the request is built Then it is rejected`() {
        assertFailsWith<IllegalArgumentException> { AutosuggestRequest(query = "restau", lang = lang) }
        assertFailsWith<IllegalArgumentException> {
            AutosuggestRequest(query = "restau", area = SearchArea.Countries(listOf("ITA")), lang = lang)
        }
    }

    @Test
    fun `Given a search centre When a circle or a bounding box is also set Then the request is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            AutosuggestRequest(query = "restau", at = milan, area = SearchArea.Circle(milan, 1_000), lang = lang)
        }
        assertFailsWith<IllegalArgumentException> {
            AutosuggestRequest(
                query = "restau",
                at = milan,
                area = SearchArea.BoundingBox(9.0, 45.0, 9.5, 45.6),
                lang = lang,
            )
        }
    }

    @Test
    fun `Given invalid values When the request is built Then it is rejected`() {
        assertFailsWith<IllegalArgumentException> { AutosuggestRequest(query = "  ", at = milan, lang = lang) }
        assertFailsWith<IllegalArgumentException> {
            AutosuggestRequest(query = "restau", at = milan, lang = emptyList())
        }
        assertFailsWith<IllegalArgumentException> {
            AutosuggestRequest(query = "restau", at = milan, lang = lang, limit = 0)
        }
        assertFailsWith<IllegalArgumentException> {
            AutosuggestRequest(query = "restau", at = milan, lang = lang, limit = 101)
        }
        assertFailsWith<IllegalArgumentException> {
            AutosuggestRequest(query = "restau", at = milan, lang = lang, offset = 100)
        }
        assertFailsWith<IllegalArgumentException> {
            AutosuggestRequest(query = "restau", at = milan, lang = lang, termsLimit = 11)
        }
    }
}
