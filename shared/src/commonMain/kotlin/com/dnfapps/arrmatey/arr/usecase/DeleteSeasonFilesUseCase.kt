package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.client.OperationStatus
import com.dnfapps.arrmatey.client.onError
import com.dnfapps.arrmatey.client.onSuccess
import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DeleteSeasonFilesUseCase {
    operator fun invoke(
        seriesId: Long,
        seasonNumber: Int,
        repository: ArrInstanceRepository
    ): Flow<OperationStatus> = flow {
        emit(OperationStatus.InProgress)
        repository.deleteSeasonFiles(seriesId, seasonNumber)
            .onSuccess {
                repository.getEpisodes(seriesId)
                emit(OperationStatus.Success("Files deleted successfully"))
            }
            .onError { code, message, cause ->
                emit(OperationStatus.Error(code, message, cause))
            }
    }
}