package com.takaotech.ktravel.domain.model

import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.Scope

@Scope(name = "PlanningScope")
data class PlanningScopeData(@InjectedParam val travelId: String)
