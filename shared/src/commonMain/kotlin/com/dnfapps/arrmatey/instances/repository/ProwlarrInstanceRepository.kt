package com.dnfapps.arrmatey.instances.repository

import com.dnfapps.arrmatey.arr.api.client.ProwlarrClient
import com.dnfapps.arrmatey.arr.api.model.ProwlarrIndexer
import com.dnfapps.arrmatey.arr.api.model.ProwlarrSearchResult
import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.instances.model.Instance
import io.ktor.client.HttpClient

class ProwlarrInstanceRepository(
    override val instance: Instance,
    httpClient: HttpClient
) : InstanceScopedRepository {

    private val prowlarrClient = ProwlarrClient(instance, httpClient)

    override suspend fun testConnection(): NetworkResult<Unit> =
        prowlarrClient.testConnection()

    suspend fun getIndexers(): NetworkResult<List<ProwlarrIndexer>> =
        prowlarrClient.getIndexers()

    suspend fun search(
        query: String,
        categories: List<Int> = emptyList(),
        indexerIds: List<Long> = emptyList()
    ): NetworkResult<List<ProwlarrSearchResult>> =
        prowlarrClient.search(query = query, categories = categories, indexerIds = indexerIds)

    suspend fun grabRelease(guid: String, indexerId: Long): NetworkResult<ProwlarrSearchResult> =
        prowlarrClient.grab(guid, indexerId)
}
