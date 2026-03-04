package com.dnfapps.arrmatey.di

import com.dnfapps.arrmatey.downloadclient.repository.DownloadClientManager
import com.dnfapps.arrmatey.downloadclient.repository.DownloadClientRepository
import com.dnfapps.arrmatey.downloadclient.usecase.CreateDownloadClientUseCase
import com.dnfapps.arrmatey.downloadclient.usecase.DeleteDownloadClientUseCase
import com.dnfapps.arrmatey.downloadclient.usecase.DeleteDownloadUseCase
import com.dnfapps.arrmatey.downloadclient.usecase.ObserveDownloadClientsUseCase
import com.dnfapps.arrmatey.downloadclient.usecase.ObserveDownloadQueueUseCase
import com.dnfapps.arrmatey.downloadclient.usecase.PauseDownloadUseCase
import com.dnfapps.arrmatey.downloadclient.usecase.ResumeDownloadUseCase
import com.dnfapps.arrmatey.downloadclient.usecase.TestDownloadClientConnectionUseCase
import com.dnfapps.arrmatey.downloadclient.usecase.UpdateDownloadClientUseCase
import com.dnfapps.arrmatey.downloadclient.viewmodel.DownloadClientSettingsViewModel
import com.dnfapps.arrmatey.downloadclient.viewmodel.DownloadQueueViewModel
import org.koin.dsl.module

val downloadClientModule = module {
    single { get<com.dnfapps.arrmatey.database.ArrMateyDatabase>().getDownloadClientDao() }

    single { DownloadClientRepository(get()) }
    single { DownloadClientManager(get(), get()) }

    factory { ObserveDownloadClientsUseCase(get()) }
    factory { ObserveDownloadQueueUseCase(get()) }
    factory { PauseDownloadUseCase(get()) }
    factory { ResumeDownloadUseCase(get()) }
    factory { DeleteDownloadUseCase(get()) }
    factory { TestDownloadClientConnectionUseCase(get()) }
    factory { CreateDownloadClientUseCase(get()) }
    factory { DeleteDownloadClientUseCase(get()) }
    factory { UpdateDownloadClientUseCase(get()) }

    factory { DownloadQueueViewModel(get(), get(), get(), get()) }
    factory { DownloadClientSettingsViewModel(get(), get(), get()) }
}
