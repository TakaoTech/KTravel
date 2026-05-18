package com.takaotech.ktravel.domain.model

import com.takaotech.ktravel.di.PlanningScope
import org.koin.core.annotation.Scope
import org.koin.core.annotation.Scoped

@Scope(PlanningScope::class)
@Scoped
class PlanningScopeData {
    lateinit var travelId: String
}
