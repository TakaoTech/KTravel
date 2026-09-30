package com.takaotech.ktravel.domain.search.adapter

import com.takaotech.ktravel.domain.search.model.PlaceDetails

/**
 * Turns one source's own description of a place into the [PlaceDetails] every screen reads.
 *
 * Each provider, and each other source of places the app has, brings its own implementation: the
 * navigator's answer for HERE, a candidate already on screen, a provider added tomorrow. Whatever
 * the source cannot say is left at the default of [PlaceDetails] rather than guessed, so a screen
 * never has to know where a place came from to draw it.
 *
 * @param T The source's own type.
 */
fun interface PlaceDetailsAdapter<in T> {

    /** The details [source] describes, in the shape shared by every provider. */
    fun adapt(source: T): PlaceDetails
}
