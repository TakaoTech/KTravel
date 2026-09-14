package com.takaotech.gunzou.here.search.common.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Pronunciations of the names of a result, for text-to-speech, only with `show=phonemes`.
 *
 * @property placeName Pronunciations of the name of the place
 * @property countryName Pronunciations of the country name
 * @property state Pronunciations of the state name
 * @property county Pronunciations of the county name
 * @property city Pronunciations of the city name
 * @property district Pronunciations of the district name
 * @property subdistrict Pronunciations of the subdistrict name
 * @property street Pronunciations of the street name
 * @property block Pronunciations of the block name
 * @property subblock Pronunciations of the sub-block name
 */
@Serializable
data class PhonemesSection(
    @SerialName("placeName") val placeName: List<Phoneme>? = null,
    @SerialName("countryName") val countryName: List<Phoneme>? = null,
    @SerialName("state") val state: List<Phoneme>? = null,
    @SerialName("county") val county: List<Phoneme>? = null,
    @SerialName("city") val city: List<Phoneme>? = null,
    @SerialName("district") val district: List<Phoneme>? = null,
    @SerialName("subdistrict") val subdistrict: List<Phoneme>? = null,
    @SerialName("street") val street: List<Phoneme>? = null,
    @SerialName("block") val block: List<Phoneme>? = null,
    @SerialName("subblock") val subblock: List<Phoneme>? = null,
)

/**
 * A phonetic transcription of a name.
 *
 * @property value Transcription in NT-SAMPA
 * @property language BCP 47 code of the language the transcription is in
 * @property preferred Whether this is the preferred pronunciation
 */
@Serializable
data class Phoneme(
    @SerialName("value") val value: String,
    @SerialName("language") val language: String? = null,
    @SerialName("preferred") val preferred: Boolean? = null,
)
