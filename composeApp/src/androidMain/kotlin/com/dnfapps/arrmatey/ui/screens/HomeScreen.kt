package com.dnfapps.arrmatey.ui.screens

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.ModalWideNavigationRail
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.compose.TabItem
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.datastore.TabPreferences
import com.dnfapps.arrmatey.entensions.BadgeContent
import com.dnfapps.arrmatey.entensions.androidIcon
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.navigation.SettingsScreen
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.navigation.DoubleBackToExit
import com.dnfapps.arrmatey.ui.tabs.ActivityTab
import com.dnfapps.arrmatey.ui.tabs.ArrTab
import com.dnfapps.arrmatey.ui.tabs.CalendarTab
import com.dnfapps.arrmatey.ui.tabs.SettingsTabNavHost
import com.dnfapps.arrmatey.utils.mokoString
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@SuppressLint("UnrememberedMutableState")
@Composable
fun HomeScreen(
    navigationManager: NavigationManager = koinInject(),
    preferencesStore: PreferencesStore = koinInject()
) {
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val drawerExtendedState by navigationManager.drawerExpandedState.collectAsStateWithLifecycle()
    val selectedDrawerTab by navigationManager.selectedDrawerTab.collectAsStateWithLifecycle()
    val selectedTab by navigationManager.selectedTab.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState { TabItem.bottomEntries.size }

    val tabPreferences by preferencesStore.tabPreferences.collectAsStateWithLifecycle(TabPreferences())
    val visibleTabs = tabPreferences.bottomTabItems
    val drawerTabs = tabPreferences.hiddenTabs

    LaunchedEffect(selectedTab) {
        pagerState.scrollToPage(selectedTab.ordinal)
    }

    LaunchedEffect(drawerState.currentValue) {
        val isInternalOpen = drawerState.currentValue == DrawerValue.Open
        if (drawerExtendedState != isInternalOpen) {
            navigationManager.setDrawerOpen(isInternalOpen)
        }
    }

    LaunchedEffect(drawerExtendedState) {
        if (drawerExtendedState && drawerState.isClosed) {
            drawerState.open()
        } else if (!drawerExtendedState && drawerState.isOpen) {
            navigationManager.setSelectedDrawerTab(null)
            drawerState.close()
        }
    }

    DoubleBackToExit()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerState = drawerState
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    NavigationDrawerItem(
                        label = { Text(mokoString(MR.strings.home)) },
                        selected = selectedDrawerTab == null,
                        icon = { Icon(Icons.Default.Home, null) },
                        onClick = {
                            scope.launch {
                                navigationManager.setDrawerOpen(false)
                                navigationManager.setSelectedDrawerTab(null)
                                drawerState.close()
                            }
                        }
                    )
                    HorizontalDivider()
                    drawerTabs.forEach { item ->
                        NavigationDrawerItem(
                            label = { Text(mokoString(item.resource)) },
                            selected = selectedDrawerTab == item,
                            icon = { Icon(item.androidIcon, null) },
                            onClick =  {
                                scope.launch {
                                    navigationManager.setDrawerOpen(false)
                                    navigationManager.setSelectedDrawerTab(item)
                                    drawerState.close()
                                }
                            }
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    HorizontalDivider()
                    NavigationDrawerItem(
                        selected = selectedDrawerTab == TabItem.SETTINGS,
                        icon = { Icon(Icons.Default.Settings, null) },
                        label = { Text(mokoString(MR.strings.settings)) },
                        onClick = {
                            scope.launch {
                                navigationManager.setDrawerOpen(true)
                                navigationManager.setSelectedDrawerTab(TabItem.SETTINGS)
                                drawerState.close()
                            }
                        }
                    )
                }
            }
        }
    ) {
        AnimatedContent(
            targetState = selectedDrawerTab == null,
            transitionSpec = {
                (fadeIn() + scaleIn(initialScale = 0.98f))
                    .togetherWith(fadeOut())
                    .using(SizeTransform(clip = false))
            },
            label = "TabTransition"
        ) { showHome ->
            if (!showHome && selectedDrawerTab != null) {
                TabItemContent(selectedDrawerTab!!)
            } else {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.weight(1f),
                        userScrollEnabled = false,
                        beyondViewportPageCount = TabItem.bottomEntries.size
                    ) { page ->
                        TabItemContent(TabItem.bottomEntries[page])
                    }

                    if (visibleTabs.size > 1) {
                        NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {
                            visibleTabs.forEach { entry ->
                                NavigationBarItem(
                                    selected = entry == selectedTab,
                                    onClick = {
                                        navigationManager.setSelectedTab(entry)
                                    },
                                    icon = {
                                        BadgedBox(
                                            badge = { BadgeContent(tabItem = entry) }
                                        ) {
                                            Icon(
                                                imageVector = entry.androidIcon,
                                                contentDescription = mokoString(entry.resource)
                                            )
                                        }
                                    },
                                    label = { Text(text = mokoString(entry.resource)) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TabItemContent(tab: TabItem) {
    when (tab) {
        TabItem.SHOWS -> ArrTab(InstanceType.Sonarr)
        TabItem.MOVIES -> ArrTab(InstanceType.Radarr)
        TabItem.MUSIC -> ArrTab(InstanceType.Lidarr)
        TabItem.ACTIVITY -> ActivityTab()
        TabItem.CALENDAR -> CalendarTab()
        TabItem.SETTINGS -> SettingsTabNavHost()
    }
}