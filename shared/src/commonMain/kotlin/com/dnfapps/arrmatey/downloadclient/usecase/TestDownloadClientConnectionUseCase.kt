package com.dnfapps.arrmatey.downloadclient.usecase

import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.downloadclient.repository.DownloadClientManager
import com.dnfapps.arrmatey.downloadclient.state.DownloadClientConnectionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TestDownloadClientConnectionUseCase(
    private val downloadClientManager: DownloadClientManager
) {

    operator fun invoke(id: Long): Flow<DownloadClientConnectionState> = flow {
        emit(DownloadClientConnectionState.Loading)

        val api = downloadClientManager.getDownloadClientApi(id)
        if (api == null) {
            emit(DownloadClientConnectionState.Error(message = "Download client cannot be found"))
            return@flow
        }

        when (val result = api.testConnection()) {
            is NetworkResult.Success -> emit(DownloadClientConnectionState.Success)
            is NetworkResult.Error -> emit(
                DownloadClientConnectionState.Error(
                    code = result.code,
                    message = result.message,
                    cause = result.cause
                )
            )
            is NetworkResult.Loading -> emit(DownloadClientConnectionState.Loading)
            else -> emit(DownloadClientConnectionState.Error(message = "Unknown response"))
        }
    }
}
