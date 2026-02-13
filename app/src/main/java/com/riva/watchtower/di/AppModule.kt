package com.riva.watchtower.di

import com.riva.watchtower.data.external.SiteTrackingProvider
import com.riva.watchtower.presentation.features.home.logic.HomeViewModel
import com.riva.watchtower.utils.HttpClientFactory
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module


val appModule = module {
    single { HttpClientFactory.create() }
    singleOf(::HomeViewModel)
    singleOf(::SiteTrackingProvider)
}