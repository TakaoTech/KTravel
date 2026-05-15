package com.takaotech.ktravel.domain.model

import org.koin.core.annotation.Scope
import org.koin.core.annotation.Scoped

@Scope(name = "PlanningScope")
@Scoped
class PlanningScopeData {
    lateinit var travelId: String
}
