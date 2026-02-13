package com.riva.watchtower.di

import androidx.room.Room
import coil3.ImageLoader
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.riva.watchtower.data.db.WatchTowerDatabase
import com.riva.watchtower.data.external.SiteTrackingProvider
import com.riva.watchtower.data.local.HtmlStorageProvider
import com.riva.watchtower.data.repository.SiteRepositoryImpl
import com.riva.watchtower.domain.repository.SiteRepository
import com.riva.watchtower.presentation.features.detail.logic.DetailViewModel
import com.riva.watchtower.presentation.features.home.logic.HomeViewModel
import com.riva.watchtower.utils.HttpClientFactory
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    single { HttpClientFactory.create() }
    single {
        ImageLoader.Builder(androidContext())
            .components {
                add(KtorNetworkFetcherFactory(httpClient = get<io.ktor.client.HttpClient>()))
            }
            .build()
    }
    singleOf(::SiteTrackingProvider)
    singleOf(::HtmlStorageProvider)
    single {
        Room.databaseBuilder(
            androidContext(),
            WatchTowerDatabase::class.java,
            "watchtower.db"
        ).build()
    }
    single { get<WatchTowerDatabase>().siteDao() }
    single<SiteRepository> { SiteRepositoryImpl(get(), get(), get()) }
    viewModelOf(::HomeViewModel)
    viewModelOf(::DetailViewModel)
}
