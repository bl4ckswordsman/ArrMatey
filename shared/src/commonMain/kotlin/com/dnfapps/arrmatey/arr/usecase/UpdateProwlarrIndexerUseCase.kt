package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.arr.api.model.ProwlarrIndexer
import com.dnfapps.arrmatey.arr.state.ProwlarrIndexersOperationState
import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class UpdateProwlarrIndexerUseCase(
    private val instanceManager: InstanceManager
) {
    operator fun invoke(instanceId: Long, indexer: ProwlarrIndexer): Flow<ProwlarrIndexersOperationState> = flow {
        emit(ProwlarrIndexersOperationState.Loading)

        val repository = instanceManager.getProwlarrRepository(instanceId)
        if (repository == null) {
            emit(ProwlarrIndexersOperationState.Error("Instance not found"))
            return@flow
        }

        when (val result = repository.updateIndexer(indexer)) {
            is NetworkResult.Success -> emit(ProwlarrIndexersOperationState.Success)
            is NetworkResult.Error -> emit(
                ProwlarrIndexersOperationState.Error(
                    result.message ?: "Failed to update indexer"
                )
            )
            is NetworkResult.Loading -> emit(ProwlarrIndexersOperationState.Loading)
            else -> emit(ProwlarrIndexersOperationState.Error("Unexpected state"))
        }
    }
}
