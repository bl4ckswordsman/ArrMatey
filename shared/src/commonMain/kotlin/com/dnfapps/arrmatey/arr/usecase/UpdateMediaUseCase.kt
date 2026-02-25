package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository

class UpdateMediaUseCase {
    suspend operator fun invoke(
        item: ArrMedia,
        repository: ArrInstanceRepository
    ): NetworkResult<ArrMedia> {
        return repository.updateMediaItem(item)
    }

    suspend fun edit(
        item: ArrMedia,
        moveFiles: Boolean,
        repository: ArrInstanceRepository
    ): NetworkResult<Unit> {
        return repository.editMediaItem(item, moveFiles)
    }
}