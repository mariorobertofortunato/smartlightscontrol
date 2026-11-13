package com.smartlightscontrol.di

import com.smartlightscontrol.data.LightService
import com.smartlightscontrol.data.LightsRepositoryImpl
import com.smartlightscontrol.data.MockLightService
import com.smartlightscontrol.domain.LightsRepository
import com.smartlightscontrol.presentation.HomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<LightService> { MockLightService() }
    single<LightsRepository> { LightsRepositoryImpl(get()) }
    viewModel { HomeViewModel(get()) }
}
