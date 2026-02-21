package com.dnfapps.arrmatey.downloadclient.usecase

import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.downloadclient.repository.DownloadClientManager
import com.dnfapps.arrmatey.downloadclient.state.DownloadQueueState
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive

class ObserveDownloadQueueUseCase(
    private val downloadClientManager: DownloadClientManager
) {

    operator fun invoke(pollingIntervalMillis: Long = 5000L): Flow<DownloadQueueState> = flow {
        emit(DownloadQueueState.Initial)

        while (currentCoroutineContext().isActive) {
            val api = downloadClientManager.getSelectedDownloadClientApiSnapshot()

            if (api == null) {
                emit(DownloadQueueState.Error(message = "No download client selected"))
                delay(pollingIntervalMillis)
                continue
            }

            emit(DownloadQueueState.Loading)

            val downloadsResult = api.getDownloads()
            val transferInfoResult = api.getTransferInfo()

            val state = when (downloadsResult) {
                is NetworkResult.Success -> {
                    when (transferInfoResult) {
                        is NetworkResult.Success -> DownloadQueueState.Success(
                            items = downloadsResult.data,
                            transferInfo = transferInfoResult.data
                        )
                        is NetworkResult.Error -> DownloadQueueState.Error(
                            code = transferInfoResult.code,
                            message = transferInfoResult.message,
                            cause = transferInfoResult.cause
                        )
                        is NetworkResult.Loading -> DownloadQueueState.Loading
                        else -> DownloadQueueState.Error(message = "Unknown transfer response")
                    }
                }
                is NetworkResult.Error -> DownloadQueueState.Error(
                    code = downloadsResult.code,
                    message = downloadsResult.message,
                    cause = downloadsResult.cause
                )
                is NetworkResult.Loading -> DownloadQueueState.Loading
                else -> DownloadQueueState.Error(message = "Unknown download response")
            }

            emit(state)
            delay(pollingIntervalMillis)
        }
    }
}
