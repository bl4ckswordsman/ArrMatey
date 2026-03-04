package com.dnfapps.arrmatey.instances.repository

import com.dnfapps.arrmatey.arr.api.client.ProwlarrClient
import com.dnfapps.arrmatey.arr.api.model.ProwlarrSearchResult
import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.instances.model.Instance
import io.ktor.client.HttpClient

class ProwlarrInstanceRepository(
    override val instance: Instance,
    httpClient: HttpClient
) : InstanceScopedRepository {

    val prowlarrClient = ProwlarrClient(instance, httpClient)

    override suspend fun testConnection(): NetworkResult<Unit> =
        prowlarrClient.testConnection()

    suspend fun grabRelease(guid: String, indexerId: Long): NetworkResult<ProwlarrSearchResult> =
        prowlarrClient.grab(guid, indexerId)
}

