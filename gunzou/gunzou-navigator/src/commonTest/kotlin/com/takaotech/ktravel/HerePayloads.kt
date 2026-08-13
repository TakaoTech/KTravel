package com.takaotech.ktravel

/**
 * Answers recorded from the HERE APIs, trimmed to the fields this server reads.
 *
 * Trimmed and not invented: every key here appears in a real payload, in the shape HERE writes it.
 * The fields that were cut are the ones the contract has no home for, and cutting them also proves
 * something worth proving — that the vendor DTOs decode a partial answer, which is what they will
 * get the day HERE stops sending something.
 */
object HerePayloads {

    /** A road route with two sections, an action, a toll and a polyline. */
    val CAR_ROUTE = """
        {
          "routes": [
            {
              "id": "route-1",
              "sections": [
                {
                  "id": "section-1",
                  "type": "vehicle",
                  "departure": {
                    "time": "2026-08-13T09:00:00+02:00",
                    "place": {
                      "type": "place",
                      "location": { "lat": 44.4949, "lng": 11.3426 },
                      "name": "Via Rizzoli"
                    }
                  },
                  "arrival": {
                    "time": "2026-08-13T09:35:00+02:00",
                    "place": {
                      "type": "place",
                      "location": { "lat": 44.1391, "lng": 11.1583 }
                    }
                  },
                  "summary": { "duration": 2100, "length": 48000, "baseDuration": 1980 },
                  "polyline": "BFoz5xJ67i1B1B7PzIhaxL7Y",
                  "transport": { "mode": "car" },
                  "actions": [
                    {
                      "action": "depart",
                      "duration": 0,
                      "length": 0,
                      "instruction": "Head south on Via Rizzoli",
                      "offset": 0
                    },
                    {
                      "action": "turn",
                      "duration": 120,
                      "length": 1400,
                      "instruction": "Turn right onto the A1",
                      "offset": 12,
                      "direction": "right",
                      "severity": "quite"
                    }
                  ],
                  "tollSystems": [ { "id": "autostrade", "name": "Autostrade per l'Italia" } ],
                  "tolls": [
                    {
                      "tollSystem": "Autostrade per l'Italia",
                      "tollSystemRef": 0,
                      "tollSystems": [ 0 ],
                      "countryCode": "ITA",
                      "tollCollectionLocations": [
                        { "name": "Barriera Bologna", "location": { "lat": 44.45, "lng": 11.30 } }
                      ],
                      "fares": [
                        {
                          "id": "fare-1",
                          "name": "Class A",
                          "price": {
                            "type": "value",
                            "currency": "EUR",
                            "value": 8.7,
                            "estimated": false
                          },
                          "paymentMethods": [ "cash", "transponder" ]
                        }
                      ]
                    }
                  ]
                },
                {
                  "id": "section-2",
                  "type": "vehicle",
                  "departure": {
                    "time": "2026-08-13T09:35:00+02:00",
                    "place": { "type": "place", "location": { "lat": 44.1391, "lng": 11.1583 } }
                  },
                  "arrival": {
                    "time": "2026-08-13T10:05:00+02:00",
                    "place": { "type": "place", "location": { "lat": 43.7696, "lng": 11.2558 } }
                  },
                  "summary": { "duration": 1800, "length": 42000, "baseDuration": 1700 },
                  "polyline": "BG6qmlDy_2xL",
                  "transport": { "mode": "car" }
                }
              ]
            }
          ],
          "notices": [
            { "title": "Route uses a toll road", "code": "violatedAvoidTollRoad", "severity": "critical" }
          ]
        }
    """.trimIndent()

    /** A road answer with a toll quoted as a range rather than a single figure. */
    val CAR_ROUTE_WITH_PRICE_RANGE = """
        {
          "routes": [
            {
              "id": "route-1",
              "sections": [
                {
                  "id": "section-1",
                  "type": "vehicle",
                  "departure": {
                    "place": { "type": "place", "location": { "lat": 44.4949, "lng": 11.3426 } }
                  },
                  "arrival": {
                    "place": { "type": "place", "location": { "lat": 43.7696, "lng": 11.2558 } }
                  },
                  "summary": { "duration": 3600, "length": 90000 },
                  "transport": { "mode": "car" },
                  "tollSystems": [ { "id": "vignette" } ],
                  "tolls": [
                    {
                      "tollSystem": "vignette",
                      "tollSystemRef": 0,
                      "fares": [
                        {
                          "id": "fare-1",
                          "name": "Vignette",
                          "price": {
                            "type": "range",
                            "currency": "CHF",
                            "minimum": 12.0,
                            "maximum": 40.0,
                            "estimated": true
                          }
                        }
                      ]
                    }
                  ]
                }
              ]
            }
          ]
        }
    """.trimIndent()

    /** A journey that walks to a station, takes a regional train, and walks off. */
    val TRANSIT_ROUTE = """
        {
          "routes": [
            {
              "id": "journey-1",
              "sections": [
                {
                  "id": "section-1",
                  "type": "pedestrian",
                  "departure": {
                    "time": "2026-08-13T09:00:00+02:00",
                    "place": { "type": "place", "location": { "lat": 44.4949, "lng": 11.3426 } }
                  },
                  "arrival": {
                    "time": "2026-08-13T09:06:00+02:00",
                    "place": {
                      "name": "Bologna Centrale",
                      "type": "station",
                      "location": { "lat": 44.5058, "lng": 11.3428 }
                    }
                  },
                  "travelSummary": { "duration": 360, "length": 420 },
                  "polyline": "BFoz5xJ67i1B1B7P"
                },
                {
                  "id": "section-2",
                  "type": "transit",
                  "departure": {
                    "time": "2026-08-13T09:14:00+02:00",
                    "platform": "5",
                    "place": {
                      "name": "Bologna Centrale",
                      "type": "station",
                      "location": { "lat": 44.5058, "lng": 11.3428 },
                      "id": "place:8300001"
                    }
                  },
                  "arrival": {
                    "time": "2026-08-13T09:41:00+02:00",
                    "place": {
                      "name": "Porretta Terme",
                      "type": "station",
                      "location": { "lat": 44.1601, "lng": 10.9739 }
                    }
                  },
                  "travelSummary": { "duration": 1620, "length": 58200 },
                  "polyline": "BG6qmlDy_2xL",
                  "transport": {
                    "mode": "regionalTrain",
                    "name": "R 2841",
                    "category": "Regionale",
                    "headsign": "Porretta Terme",
                    "shortName": "R",
                    "color": "#008C45",
                    "textColor": "#FFFFFF"
                  },
                  "intermediateStops": [
                    {
                      "departure": {
                        "time": "2026-08-13T09:22:00+02:00",
                        "place": {
                          "name": "Casalecchio Garibaldi",
                          "type": "station",
                          "location": { "lat": 44.4795, "lng": 11.2764 }
                        }
                      }
                    }
                  ]
                }
              ]
            }
          ]
        }
    """.trimIndent()

    /** What HERE answers when the key is not accepted. */
    val UNAUTHORIZED = """
        {
          "error": "Unauthorized",
          "error_description": "Token Validation Failure - invalid credentials",
          "title": "Unauthorized",
          "status": 401,
          "cause": "Token Validation Failure - invalid credentials"
        }
    """.trimIndent()

    /** An answer with no route in it, which HERE reports as a success. */
    const val NO_ROUTES = """{ "routes": [] }"""
}
