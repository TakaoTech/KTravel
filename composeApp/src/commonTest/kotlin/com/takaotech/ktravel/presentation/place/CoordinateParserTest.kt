package com.takaotech.ktravel.presentation.place

import com.takaotech.ktravel.domain.search.model.GeoCoordinate
import com.takaotech.ktravel.domain.search.model.PlaceCandidateSource
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class CoordinateParserTest :
    BehaviorSpec({

        given("a pair of degrees typed in the search bar") {
            listOf(
                "45.1334, 10.0246" to GeoCoordinate(45.1334, 10.0246),
                "45.1334,10.0246" to GeoCoordinate(45.1334, 10.0246),
                "45.1334 10.0246" to GeoCoordinate(45.1334, 10.0246),
                "45.1334; 10.0246" to GeoCoordinate(45.1334, 10.0246),
                "  -33.8568 , 151.2153  " to GeoCoordinate(-33.8568, 151.2153),
                "+90, -180" to GeoCoordinate(90.0, -180.0),
                "0 0" to GeoCoordinate(0.0, 0.0),
            ).forEach { (text, expected) ->
                `when`("the text is '$text'") {
                    then("it is read as $expected") {
                        CoordinateParser.parse(text) shouldBe expected
                    }
                }
            }
        }

        given("a text that is not a pair of degrees in range") {
            listOf(
                "",
                "Torrazzo di Cremona",
                "Via Roma 12",
                "45.1334",
                "45.1, 10.2, 3",
                "90.5, 10",
                "45, 180.1",
                "45,1; 10,2",
            ).forEach { text ->
                `when`("the text is '$text'") {
                    then("no coordinate is read") {
                        CoordinateParser.parse(text) shouldBe null
                    }
                }
            }
        }

        given("a coordinate") {
            `when`("it becomes a candidate") {
                val candidate = CoordinateParser.candidateFor(GeoCoordinate(45.26, -9.3))

                then("it is named after the coordinate with five decimals") {
                    candidate.title shouldBe "45.26000, -9.30000"
                }

                then("its source is the typed coordinates") {
                    candidate.source shouldBe PlaceCandidateSource.COORDINATES
                    candidate.coordinate shouldBe GeoCoordinate(45.26, -9.3)
                }
            }

            `when`("a tiny negative value is formatted") {
                then("no negative zero is written") {
                    CoordinateParser.format(GeoCoordinate(-0.000001, 0.0)) shouldBe "0.00000, 0.00000"
                }
            }
        }
    })
