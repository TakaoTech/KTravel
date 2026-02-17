package com.takaotech.ktravel.presentation.planner

import androidx.lifecycle.ViewModel
import com.takaotech.ktravel.domain.repository.TravelPlanRepository
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam

@KoinViewModel
class PlanningTransportViewModel(
    @InjectedParam private val dayId: String,
    private val repository: TravelPlanRepository
) : ViewModel()
