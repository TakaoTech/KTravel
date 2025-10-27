package com.takaotech.ktravel.domain.model

import kotlinx.datetime.LocalTime

abstract class TNav(
    open val id: Int,
) {
    data class Hours(
        val startHours: LocalTime? = null,
        val endHours: LocalTime? = null,
    )
}

data class TPlace(
    override val id: Int,
    val name: String,
    val position: Pair<Long, Long>
) : TNav(id = id)

data class TTransport(
    override val id: Int,
    override val departure: Transport.Position,
    override val arrival: Transport.Position
) : TNav(id = id), Transport
