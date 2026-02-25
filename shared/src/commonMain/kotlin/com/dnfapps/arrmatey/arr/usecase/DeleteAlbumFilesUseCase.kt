package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.client.OperationStatus
import com.dnfapps.arrmatey.client.onError
import com.dnfapps.arrmatey.client.onSuccess
import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DeleteAlbumFilesUseCase {
    operator fun invoke(
        artistId: Long,
        albumId: Long,
        repository: ArrInstanceRepository
    ): Flow<OperationStatus> = flow {
        emit(OperationStatus.InProgress)
        repository.deleteAlbumFiles(artistId, albumId)
            .onSuccess {
                emit(OperationStatus.Success(message = "Files deleted successfully"))
            }
            .onError { code, message, cause ->
                emit(OperationStatus.Error(code, message, cause))
            }
    }
}