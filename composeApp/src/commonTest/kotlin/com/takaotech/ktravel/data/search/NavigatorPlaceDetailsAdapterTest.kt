package com.takaotech.ktravel.data.search

import com.takaotech.gunzou.api.common.GeoPoint
import com.takaotech.gunzou.api.common.SearchProviderId
import com.takaotech.gunzou.api.search.PlaceCategoryGroup
import com.takaotech.gunzou.api.search.SearchAddress
import com.takaotech.gunzou.api.search.SearchResultType
import com.takaotech.gunzou.api.search.place.ContactKind
import com.takaotech.gunzou.api.search.place.PermanentClosure
import com.takaotech.gunzou.api.search.place.PlaceTimeZone
import com.takaotech.gunzou.api.search.place.Weekday
import com.takaotech.ktravel.domain.search.model.GeoCoordinate
import com.takaotech.ktravel.domain.search.model.OpeningPeriod
import com.takaotech.ktravel.domain.search.model.PlaceCategory
import com.takaotech.ktravel.domain.search.model.PlaceClosure
import com.takaotech.ktravel.domain.search.model.PlaceContact
import com.takaotech.ktravel.domain.search.model.PlaceContactKind
import com.takaotech.ktravel.domain.search.model.PlaceReference
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlin.time.Duration.Companion.minutes
import com.takaotech.gunzou.api.search.place.OpeningPeriod as ApiOpeningPeriod
import com.takaotech.gunzou.api.search.place.PlaceContact as ApiPlaceContact
import com.takaotech.gunzou.api.search.place.PlaceDetails as ApiPlaceDetails

private const val PLACE_ID = "here:pds:place:380sr2yk-0f0d8a4b5e0b4b1f9f"

private val adapter = NavigatorPlaceDetailsAdapter(SearchProviderId.Here)

private val restaurant = ApiPlaceDetails(
    id = PLACE_ID,
    title = "Trattoria Da Enzo",
    resultType = SearchResultType.PLACE,
    position = GeoPoint(lat = 41.88845, lng = 12.47697),
    address = SearchAddress(label = "Via dei Vascellari 29, 00153 Roma RM, Italia", county = "Roma", state = "Lazio"),
    category = PlaceCategoryGroup.EAT_AND_DRINK,
    contacts = listOf(
        ApiPlaceContact(kind = ContactKind.PHONE, value = "+39 06 581 2260"),
        ApiPlaceContact(kind = ContactKind("fax"), value = "+39 06 581 0000"),
        ApiPlaceContact(kind = ContactKind.WEBSITE, value = "https://www.daenzoal29.com", label = "Official"),
    ),
    openingHours = listOf(
        ApiOpeningPeriod(days = listOf(Weekday.MONDAY, Weekday("holiday")), opensAt = "12:30", durationMinutes = 150),
        ApiOpeningPeriod(days = listOf(Weekday("holiday")), opensAt = "10:00", durationMinutes = 60),
        ApiOpeningPeriod(days = listOf(Weekday.FRIDAY), opensAt = "noon", durationMinutes = 60),
    ),
    timeZone = PlaceTimeZone(name = "Europe/Rome", utcOffset = "+02:00"),
    permanentClosure = PermanentClosure.POSSIBLE,
)

class NavigatorPlaceDetailsAdapterTest :
    BehaviorSpec({

        given("the details of a restaurant from the navigator") {
            `when`("they are translated for the domain") {
                val details = adapter.adapt(restaurant)

                then("the place keeps its name, position, address and category") {
                    details.title shouldBe "Trattoria Da Enzo"
                    details.coordinate shouldBe GeoCoordinate(lat = 41.88845, lng = 12.47697)
                    details.addressLabel shouldBe "Via dei Vascellari 29, 00153 Roma RM, Italia"
                    details.locality shouldBe "Roma"
                    details.category shouldBe PlaceCategory.EAT_AND_DRINK
                }

                then("the contacts of a kind this build knows are kept, in order") {
                    details.contacts shouldBe listOf(
                        PlaceContact(kind = PlaceContactKind.PHONE, value = "+39 06 581 2260"),
                        PlaceContact(
                            kind = PlaceContactKind.WEBSITE,
                            value = "https://www.daenzoal29.com",
                            label = "Official",
                        ),
                    )
                }

                then("only the periods it can read survive, without the days it does not know") {
                    details.openingHours shouldBe listOf(
                        OpeningPeriod(
                            days = persistentSetOf(DayOfWeek.MONDAY),
                            opensAt = LocalTime(hour = 12, minute = 30),
                            duration = 150.minutes,
                        ),
                    )
                }

                then("the time zone and the suspected closure come through") {
                    details.timeZoneId shouldBe "Europe/Rome"
                    details.closure shouldBe PlaceClosure.POSSIBLE
                }

                then("it remembers the provider that can be asked about the place again") {
                    details.reference shouldBe PlaceReference(providerId = "here", placeId = PLACE_ID)
                }
            }
        }

        given("the details of a street, with nothing but an address") {
            val street = ApiPlaceDetails(
                id = "here:af:street:1",
                title = "Via del Corso",
                resultType = SearchResultType.STREET,
                position = GeoPoint(lat = 41.9, lng = 12.48),
                category = PlaceCategoryGroup.TRANSPORT,
                permanentClosure = PermanentClosure("temporary"),
            )

            `when`("they are translated for the domain") {
                val details = adapter.adapt(street)

                then("a group the trip does not filter by and an unknown closure are both absent") {
                    details.category.shouldBeNull()
                    details.closure.shouldBeNull()
                    details.contacts.shouldBeEmpty()
                    details.openingHours.shouldBeEmpty()
                }
            }
        }
    })
