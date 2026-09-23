package com.takaotech.gunzou.here.search.revgeocode.dto.request

import com.takaotech.gunzou.here.common.model.Coordinate
import com.takaotech.gunzou.here.search.common.model.SearchArea
import com.takaotech.gunzou.here.search.revgeocode.model.RevgeocodeType
import com.vanniktech.locale.Locale
import kotlin.test.Test
import kotlin.test.assertFailsWith

class RevgeocodeRequestTest {

    private val lang = listOf(Locale.from("en-US"))
    private val milan = Coordinate(45.4642, 9.19)
    private val circle = SearchArea.Circle(milan, 1_000)

    @Test
    fun `Given a point or a circle When the request is built Then it is accepted`() {
        RevgeocodeRequest(at = milan, lang = lang)
        RevgeocodeRequest(area = circle, lang = lang)
    }

    @Test
    fun `Given both a point and a circle or neither When the request is built Then it is rejected`() {
        assertFailsWith<IllegalArgumentException> { RevgeocodeRequest(at = milan, area = circle, lang = lang) }
        assertFailsWith<IllegalArgumentException> { RevgeocodeRequest(lang = lang) }
    }

    @Test
    fun `Given a bearing When the types are only streets Then the request is accepted`() {
        RevgeocodeRequest(at = milan, bearing = 0, lang = lang)
        RevgeocodeRequest(at = milan, bearing = 359, types = listOf(RevgeocodeType.STREET), lang = lang)
    }

    @Test
    fun `Given a bearing When another type is requested Then the request is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            RevgeocodeRequest(
                at = milan,
                bearing = 90,
                types = listOf(RevgeocodeType.STREET, RevgeocodeType.ADDRESS),
                lang = lang,
            )
        }
    }

    @Test
    fun `Given invalid values When the request is built Then it is rejected`() {
        assertFailsWith<IllegalArgumentException> { RevgeocodeRequest(at = milan, bearing = -1, lang = lang) }
        assertFailsWith<IllegalArgumentException> { RevgeocodeRequest(at = milan, bearing = 360, lang = lang) }
        assertFailsWith<IllegalArgumentException> { RevgeocodeRequest(at = milan, lang = emptyList()) }
        assertFailsWith<IllegalArgumentException> { RevgeocodeRequest(at = milan, lang = lang, limit = 0) }
        assertFailsWith<IllegalArgumentException> { RevgeocodeRequest(at = milan, lang = lang, limit = 101) }
    }
}
