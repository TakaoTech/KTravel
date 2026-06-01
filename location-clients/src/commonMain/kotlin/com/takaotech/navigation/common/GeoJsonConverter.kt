package com.takaotech.navigation.common

import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

object GeoJsonConverter {

    fun polylineToGeoJson(encodedPolyline: String): String =
        buildLineStringGeoJson(PolylineEncoderDecoder.decode(encodedPolyline))

    fun mergePolylinesToGeoJson(encodedPolylines: List<String>): String {
        require(encodedPolylines.isNotEmpty()) { "encodedPolylines must not be empty" }
        val coordinates = encodedPolylines.flatMap { PolylineEncoderDecoder.decode(it) }
        return buildLineStringGeoJson(coordinates)
    }

    private fun buildLineStringGeoJson(coordinates: List<PolylineEncoderDecoder.LatLngZ>): String {
        val coordsArray = buildJsonArray {
            coordinates.forEach { point ->
                // GeoJSON coordinate order is [longitude, latitude] per RFC 7946
                add(buildJsonArray {
                    add(JsonPrimitive(point.lng))
                    add(JsonPrimitive(point.lat))
                })
            }
        }
        return buildJsonObject {
            put("type", "Feature")
            putJsonObject("geometry") {
                put("type", "LineString")
                put("coordinates", coordsArray)
            }
            putJsonObject("properties") {}
        }.toString()
    }
}
