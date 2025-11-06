package com.takaotech.ktravel.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.ksp.generated.module

@Module
@ComponentScan("com.takaotech.ktravel")
class AppModule

fun appModule() = listOf(AppModule().module)
