package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.client.OperationStatus
import com.dnfapps.arrmatey.client.onError
import com.dnfapps.arrmatey.client.onSuccess
import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DeleteEpisodeFileUseCase {
    operator fun invoke(
        seriesId: Long,
        episodeFileId: Long,
        repository: ArrInstanceRepository
    ): Flow<OperationStatus> = flow {
        emit(OperationStatus.InProgress)
        repository.deleteEpisodeFile(seriesId, episodeFileId)
            .onSuccess {
                emit(OperationStatus.Success("Episode deleted successfully"))
            }
            .onError { code, message, cause ->
                emit(OperationStatus.Error(code, message, cause))
            }
    }
}