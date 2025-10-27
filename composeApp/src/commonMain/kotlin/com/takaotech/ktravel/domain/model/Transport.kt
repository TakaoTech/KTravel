package com.takaotech.ktravel.domain.model

interface Transport {
    val departure: Position
    val arrival: Position

    data class Position(val lat: Long, val lng: Long)

    enum class Type{
        TRAIN,
        AIRPLANE,
        BOAT
    }
}