package com.dnfapps.arrmatey.ui.tabs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.dnfapps.arrmatey.arr.viewmodel.ProwlarrIndexersViewModel
import com.dnfapps.arrmatey.arr.viewmodel.ProwlarrSearchViewModel
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.navigation.NavigationDrawerButton
import com.dnfapps.arrmatey.ui.screens.ProwlarrIndexersContent
import com.dnfapps.arrmatey.ui.screens.ProwlarrSearchContent
import com.dnfapps.arrmatey.utils.mokoString
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProwlarrTab(
    indexersViewModel: ProwlarrIndexersViewModel = koinInject(),
    searchViewModel: ProwlarrSearchViewModel = koinInject()
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf(mokoString(MR.strings.indexers), mokoString(MR.strings.search))

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(mokoString(MR.strings.prowlarr)) },
                    navigationIcon = { NavigationDrawerButton() },
                    actions = {
                        if (selectedTabIndex == 0) {
                            IconButton(onClick = { indexersViewModel.refresh() }) {
                                Icon(Icons.Default.Refresh, contentDescription = mokoString(MR.strings.refresh))
                            }
                        }
                    }
                )
                TabRow(selectedTabIndex = selectedTabIndex) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) }
                        )
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets.statusBars
    ) { paddingValues ->
        when (selectedTabIndex) {
            0 -> ProwlarrIndexersContent(
                viewModel = indexersViewModel,
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            )
            else -> ProwlarrSearchContent(
                viewModel = searchViewModel,
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            )
        }
    }
}
