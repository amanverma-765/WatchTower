package com.riva.watchtower.di

import com.riva.watchtower.utils.HttpClientFactory
import org.koin.dsl.module


val appModule = module {
    single { HttpClientFactory.create() }


}