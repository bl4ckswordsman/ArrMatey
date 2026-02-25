package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.client.OperationStatus
import com.dnfapps.arrmatey.client.onError
import com.dnfapps.arrmatey.client.onSuccess
import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DeleteMediaUseCase {
    operator fun invoke(
        mediaId: Long,
        deleteFiles: Boolean,
        addImportExclusion: Boolean,
        repository: ArrInstanceRepository
    ): Flow<OperationStatus> = flow {
        emit(OperationStatus.InProgress)
        repository.delete(mediaId, deleteFiles, addImportExclusion)
            .onSuccess {
                emit(OperationStatus.Success("Deleted successfully"))
            }
            .onError { code, message, cause ->
                emit(OperationStatus.Error(code, message, cause))
            }
    }
}