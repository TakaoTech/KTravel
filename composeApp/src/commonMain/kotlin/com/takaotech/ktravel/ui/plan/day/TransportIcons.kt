package com.takaotech.ktravel.ui.plan.day

import com.takaotech.ktravel.domain.model.TransportType
import io.nacular.measured.units.times
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.directions_bus
import ktravel.composeapp.generated.resources.directions_car
import ktravel.composeapp.generated.resources.flight
import ktravel.composeapp.generated.resources.train
import org.jetbrains.compose.resources.DrawableResource

fun TransportType.toIcon(): DrawableResource = when (this) {
    TransportType.TRAIN -> Res.drawable.train
    TransportType.BUS -> Res.drawable.directions_bus
    TransportType.CAR -> Res.drawable.directions_car
    TransportType.FLIGHT -> Res.drawable.flight
}
