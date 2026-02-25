package com.dnfapps.arrmatey.client.paging

import kotlinx.serialization.Serializable

@Serializable
data class PageInfo(
    val page: Int,
    val pages: Int,
    val pageSize: Int,
    val results: Int
)