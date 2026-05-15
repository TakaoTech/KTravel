package com.takaotech.ktravel.domain.usecase

import com.takaotech.ktravel.domain.model.StepDomain
import com.takaotech.ktravel.domain.model.TransportType
import com.takaotech.ktravel.domain.repository.TravelPlanRepository
import com.takaotech.ktravel.domain.routing.model.Route
import org.koin.core.annotation.Scope
import org.koin.core.annotation.Scoped

@Scope(name = "PlanningScope")
@Scoped
class SaveTransportStepUseCase(
    private val repository: TravelPlanRepository
) {
    suspend operator fun invoke(dayId: String, afterStepId: String, route: Route) {
        val transportType = route.sections.firstOrNull()?.transport?.mode.toTransportType()
        val step = StepDomain.Transport(type = transportType, route = route)
        repository.addTransportStep(dayId, afterStepId, step)
    }

    private fun String?.toTransportType(): TransportType = when (this?.uppercase()) {
        "TRAIN" -> TransportType.TRAIN
        "BUS" -> TransportType.BUS
        "FLIGHT" -> TransportType.FLIGHT
        else -> TransportType.CAR
    }
}
