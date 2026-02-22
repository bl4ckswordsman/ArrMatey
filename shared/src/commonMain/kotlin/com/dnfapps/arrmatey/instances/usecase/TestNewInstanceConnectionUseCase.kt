package com.dnfapps.arrmatey.instances.usecase

import com.dnfapps.arrmatey.arr.api.client.GenericClient
import com.dnfapps.arrmatey.instances.model.InstanceType

class TestNewInstanceConnectionUseCase(
    private val client: GenericClient
) {
    suspend operator fun invoke(url: String, apiKey: String, type: InstanceType): Boolean =
        client.test(url.trim(), apiKey.trim(), type)
}
