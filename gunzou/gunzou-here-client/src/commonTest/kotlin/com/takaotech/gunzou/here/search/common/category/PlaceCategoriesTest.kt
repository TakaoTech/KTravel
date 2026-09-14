package com.takaotech.gunzou.here.search.common.category

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class PlaceCategoriesTest {

    private val subgroups = PlaceCategories.groups.flatMap { it.subgroups }

    @Test
    fun `Given the category tree When it is counted Then it matches the HERE documentation`() {
        assertEquals(11, PlaceCategories.groups.size)
        assertEquals(61, subgroups.size)
        assertEquals(437, PlaceCategories.leaves.size)
    }

    @Test
    fun `Given every category When their ids are collected Then no id is repeated`() {
        val ids = PlaceCategories.groups.map { it.id } + subgroups.map { it.id } + PlaceCategories.leaves.map { it.id }

        assertEquals(
            ids.size,
            ids.toSet().size,
            "duplicated ids: ${ids.groupBy { it }.filterValues { it.size > 1 }.keys}",
        )
    }

    @Test
    fun `Given every subgroup and leaf When their parents are read Then each id extends the id of its parent`() {
        PlaceCategories.groups.forEach { group ->
            group.subgroups.forEach { subgroup ->
                assertSame(group, subgroup.group, "${subgroup.id} points at the wrong group")
                assertTrue(subgroup.id.startsWith("${group.id}-"), "${subgroup.id} is not under ${group.id}")

                subgroup.categories.forEach { leaf ->
                    assertSame(subgroup, leaf.subgroup, "${leaf.id} points at the wrong subgroup")
                    assertTrue(leaf.id.startsWith("${subgroup.id}-"), "${leaf.id} is not under ${subgroup.id}")
                }
            }
        }
    }

    @Test
    fun `Given an id of each level When it is resolved Then the matching nested object is returned`() {
        assertSame(EatAndDrink, PlaceCategories.fromId("100"))
        assertSame(EatAndDrink.Restaurant, PlaceCategories.fromId("100-1000"))
        assertSame(EatAndDrink.Restaurant.CASUAL_DINING, PlaceCategories.fromId("100-1000-0001"))
        assertSame(AreasAndBuildings.CityTownOrVillage, PlaceCategories.fromId("900-9100"))
    }

    @Test
    fun `Given a leaf reached through the nested objects When its parents are read Then they are the enclosing objects`() {
        val leaf = EatAndDrink.Restaurant.CASUAL_DINING

        assertEquals("100-1000-0001", leaf.id)
        assertEquals("Casual Dining", leaf.name)
        assertSame(EatAndDrink.Restaurant, leaf.subgroup)
        assertSame(EatAndDrink, leaf.subgroup.group)
    }

    @Test
    fun `Given an id HERE added after this table When it is resolved Then null is returned`() {
        assertNull(PlaceCategories.fromId("100-1000-9999"))
        assertNull(PlaceCategories.fromId(""))
    }
}
