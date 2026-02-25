package com.dnfapps.arrmatey.datastore

import com.dnfapps.arrmatey.compose.TabItem
import kotlinx.serialization.Serializable

@Serializable
data class TabPreferences(
    val bottomTabItems: List<TabItem> = TabItem.defaultEntries,
    val hiddenTabs: List<TabItem> = TabItem.defaultHidden
) {
    constructor(): this(TabItem.defaultEntries, TabItem.defaultHidden)
}