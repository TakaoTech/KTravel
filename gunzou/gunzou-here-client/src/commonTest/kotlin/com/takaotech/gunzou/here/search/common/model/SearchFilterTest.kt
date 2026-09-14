package com.takaotech.gunzou.here.search.common.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SearchFilterTest {

    @Test
    fun `Given included and excluded ids When converted Then the excluded ones are prefixed with an exclamation mark`() {
        val filter = SearchFilter(include = listOf("100-1000"), exclude = listOf("100-1000-0009", "100-1100"))

        assertEquals("100-1000,!100-1000-0009,!100-1100", filter.toQueryString { it })
    }

    @Test
    fun `Given only exclusions When converted Then every id is prefixed`() {
        assertEquals("!a,!b", SearchFilter(include = emptyList(), exclude = listOf("a", "b")).toQueryString { it })
    }

    @Test
    fun `Given no ids at all When the filter is built Then it is rejected`() {
        assertFailsWith<IllegalArgumentException> { SearchFilter<String>(include = emptyList()) }
    }
}
