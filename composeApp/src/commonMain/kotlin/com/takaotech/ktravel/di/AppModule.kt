package com.takaotech.ktravel.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.ksp.generated.module

@Module
@ComponentScan("com.takaotech.ktravel.data")
class DataModule

@Module(includes = [DataModule::class])
@ComponentScan("com.takaotech.ktravel.domain")
class DomainModule

@Module(includes = [DomainModule::class])
@ComponentScan("com.takaotech.ktravel.presentation")
class PresentationModule

fun appModule() = listOf(
    PresentationModule().module
)