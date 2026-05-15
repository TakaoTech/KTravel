package com.takaotech.ktravel.di

import com.takaotech.ktravel.core.platformModules
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.KoinApplication
import org.koin.core.annotation.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.includes
import org.koin.plugin.module.dsl.startKoin

@Module
@ComponentScan("com.takaotech.ktravel.data")
@Configuration
class DataModule

@Module(includes = [DataModule::class])
@ComponentScan("com.takaotech.ktravel.domain")
@Configuration
class DomainModule

@Module(includes = [DomainModule::class])
@ComponentScan("com.takaotech.ktravel.presentation")
@Configuration
class PresentationModule

@KoinApplication(modules = [PresentationModule::class])
class KTravelKoinApp

fun startKTravelKoin(config: KoinAppDeclaration? = null): org.koin.core.KoinApplication {
    return startKoin<KTravelKoinApp> {
        includes(config)
        platformModules()
    }
}