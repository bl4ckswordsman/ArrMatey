package com.dnfapps.arrmatey.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.compose.TabItem
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.entensions.androidIcon
import com.dnfapps.arrmatey.navigation.Navigation
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.navigation.SettingsScreen
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.ContainerCard
import com.dnfapps.arrmatey.ui.components.navigation.BackButton
import com.dnfapps.arrmatey.utils.MokoStrings
import com.dnfapps.arrmatey.utils.mokoString
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabCustomizationScreen(
    preferenceStore: PreferencesStore = koinInject(),
    navigationManager: NavigationManager = koinInject(),
    navigation: Navigation<SettingsScreen> = navigationManager.settings(),
    moko: MokoStrings = koinInject()
) {
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val preferences by preferenceStore.tabPreferences.collectAsStateWithLifecycle(initialValue = null)

    var bottomBarTabs by remember { mutableStateOf<List<TabItem>>(emptyList()) }
    var hiddenTabs by remember { mutableStateOf<List<TabItem>>(emptyList()) }

    LaunchedEffect(preferences) {
        if (preferences != null) {
            bottomBarTabs = preferences!!.bottomTabItems
            hiddenTabs = preferences!!.hiddenTabs
        }
    }

    val combinedList = remember(bottomBarTabs, hiddenTabs) {
        buildList {
            add(TabRow.InfoCard)
            add(TabRow.Header(moko.getString(MR.strings.navigation_items_selected)))
            addAll(bottomBarTabs.map { TabRow.Tab(it, isActive = true) })
            add(TabRow.Divider)
            add(TabRow.Header(moko.getString(MR.strings.navigation_items_drawer)))

            if (hiddenTabs.isEmpty()) {
                add(TabRow.Placeholder)
            } else {
                addAll(hiddenTabs.map { TabRow.Tab(it, isActive = false) })
            }
        }
    }

    val listState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(listState) { from, to ->
        val fromRow = combinedList.getOrNull(from.index) as? TabRow.Tab ?: return@rememberReorderableLazyListState
        val movingTab = fromRow.item
        val dividerIndex = bottomBarTabs.size + 2

        val newBottomTabs = bottomBarTabs.toMutableList()
        val newHiddenTabs = hiddenTabs.toMutableList()

        val wasActive = newBottomTabs.contains(movingTab)

        if (to.index < dividerIndex) {
            if (newBottomTabs.size < 5 || wasActive) {
                if (!wasActive) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                newBottomTabs.remove(movingTab)
                newHiddenTabs.remove(movingTab)
                val insertIdx = (to.index - 1).coerceIn(0, newBottomTabs.size)
                newBottomTabs.add(insertIdx, movingTab)
            }
        } else {
            if (newBottomTabs.size > 1 || !wasActive) {
                if (wasActive) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                newBottomTabs.remove(movingTab)
                newHiddenTabs.remove(movingTab)
                val offset = newBottomTabs.size + 4
                val insertIdx = (to.index - offset).coerceIn(0, newHiddenTabs.size)
                newHiddenTabs.add(insertIdx, movingTab)
            }
        }
        bottomBarTabs = newBottomTabs
        hiddenTabs = newHiddenTabs
    }

    LaunchedEffect(reorderableState.isAnyItemDragging) {
        if (!reorderableState.isAnyItemDragging && bottomBarTabs.isNotEmpty()) {
            preferenceStore.updateBottomBarTabs(bottomBarTabs)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(mokoString(MR.strings.customize_navigation)) },
                navigationIcon = { BackButton(navigation) },
                actions = {
                    IconButton(onClick = { scope.launch { preferenceStore.resetTabPreferences() } }) {
                        Icon(Icons.Default.RestartAlt, null)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(combinedList, key = { _, row -> row.key }) { index, row ->
                when (row) {
                    is TabRow.Header -> Text(
                        text = row.text,
                        style = MaterialTheme.typography.titleMedium
                    )
                    is TabRow.Tab -> {
                        ReorderableItem(reorderableState, key = row.key) { isDragging ->
                            val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp)

                            val isBelowDivider = index > bottomBarTabs.size + 1
                            val ghostAlpha by animateFloatAsState(if (isBelowDivider) 0.6f else 1f)
                            val ghostScale by animateFloatAsState(if (isBelowDivider) 0.95f else 1f)

                            Box(modifier = Modifier
                                .graphicsLayer {
                                    alpha = ghostAlpha
                                    scaleX = ghostScale
                                    scaleY = ghostScale
                                }
                            ) {
                                TabItemCard(
                                    modifier = Modifier.draggableHandle(),
                                    tab = row.item,
                                    isDragging = isDragging,
                                    elevation = elevation
                                )
                            }
                        }
                    }
                    TabRow.Divider -> HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    TabRow.Placeholder -> {
                        ReorderableItem(reorderableState, key = row.key) { _ ->
                            Spacer(modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                            )
                        }
                    }
                    TabRow.InfoCard -> {
                        ContainerCard(
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = mokoString(MR.strings.customize_navigation_description),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabItemCard(
    tab: TabItem,
    isDragging: Boolean,
    elevation: Dp,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                translationY = if (isDragging) 4.dp.toPx() else 0f
            },
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = tab.androidIcon,
                    contentDescription = null
                )
                Text(
                    text = mokoString(tab.resource),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

sealed class TabRow(val key: String) {
    data class Header(val text: String): TabRow(text)
    data class Tab(val item: TabItem, val isActive: Boolean): TabRow(item.name)
    object Divider: TabRow("divider_key")
    object Placeholder: TabRow("placeholder_key")
    object InfoCard: TabRow("info_card_key")
}