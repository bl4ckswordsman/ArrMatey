package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.arr.state.HistoryState
import com.dnfapps.arrmatey.client.onError
import com.dnfapps.arrmatey.client.onSuccess
import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetEpisodeHistoryUseCase {
    operator fun invoke(
        episodeId: Long,
        repository: ArrInstanceRepository
    ): Flow<HistoryState> = flow {
        emit(HistoryState.Loading)
        repository.getItemHistory(episodeId)
            .onSuccess { emit(HistoryState.Success(it)) }
            .onError { _, message, _ ->
                emit(HistoryState.Error(message))
            }
    }
}