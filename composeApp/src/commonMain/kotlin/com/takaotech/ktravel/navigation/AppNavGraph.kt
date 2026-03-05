package com.takaotech.ktravel.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.takaotech.ktravel.ui.planning.navigation.planningNavGraph

fun NavGraphBuilder.appNavGraph(navController: NavHostController) {
    planningNavGraph(navController)
}
