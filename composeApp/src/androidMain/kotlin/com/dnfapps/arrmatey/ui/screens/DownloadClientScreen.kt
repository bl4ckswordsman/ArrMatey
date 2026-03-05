package com.dnfapps.arrmatey.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.downloadclient.model.DownloadClient
import com.dnfapps.arrmatey.downloadclient.state.DownloadClientConnectionState
import com.dnfapps.arrmatey.downloadclient.state.DownloadClientMutationState
import com.dnfapps.arrmatey.downloadclient.viewmodel.DownloadClientSettingsViewModel
import com.dnfapps.arrmatey.navigation.Navigation
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.navigation.SettingsScreen
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.navigation.BackButton
import com.dnfapps.arrmatey.utils.mokoString
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadClientScreen(
    viewModel: DownloadClientSettingsViewModel = koinInject(),
    navigationManager: NavigationManager = koinInject(),
    navigation: Navigation<SettingsScreen> = navigationManager.settings()
) {
    val downloadClients by viewModel.downloadClients.collectAsStateWithLifecycle()
    val connectionStates by viewModel.connectionStates.collectAsStateWithLifecycle()
    val mutationState by viewModel.mutationState.collectAsStateWithLifecycle()

    var deleteTarget by remember { mutableStateOf<DownloadClient?>(null) }

    LaunchedEffect(mutationState) {
        if (mutationState is DownloadClientMutationState.Success) {
            deleteTarget = null
            viewModel.resetMutationState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val suffix = if (downloadClients.isNotEmpty()) " (${downloadClients.size})" else ""
                    Text(text = mokoString(MR.strings.download_client) + suffix)
                },
                navigationIcon = {
                    BackButton(navigation)
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navigation.navigateTo(SettingsScreen.AddDownloadClient) }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = mokoString(MR.strings.add_download_client)
                )
            }
        },
        contentWindowInsets = WindowInsets.statusBars
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 12.dp)
        ) {
            if (mutationState is DownloadClientMutationState.Error) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Text(
                        text = (mutationState as DownloadClientMutationState.Error).message,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            if (downloadClients.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = mokoString(MR.strings.no_download_clients))
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(downloadClients, key = { it.id }) { downloadClient ->
                        DownloadClientItem(
                            downloadClient = downloadClient,
                            connectionState = connectionStates[downloadClient.id] ?: DownloadClientConnectionState.Initial,
                            onTestConnection = { viewModel.testConnection(downloadClient.id) },
                            onDelete = { deleteTarget = downloadClient },
                            onEdit = { navigation.navigateTo(SettingsScreen.EditDownloadClient(downloadClient.id)) }
                        )
                    }
                }
            }
        }

        deleteTarget?.let { downloadClient ->
            AlertDialog(
                onDismissRequest = { deleteTarget = null },
                confirmButton = {
                    TextButton(
                        onClick = { viewModel.deleteClient(downloadClient) }
                    ) {
                        Text(mokoString(MR.strings.yes))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { deleteTarget = null }
                    ) {
                        Text(mokoString(MR.strings.no))
                    }
                },
                title = {
                    Text(mokoString(MR.strings.confirm))
                },
                text = {
                    Text(mokoString(MR.strings.confirm_delete_instance, downloadClient.label))
                }
            )
        }
    }
}

@Composable
private fun DownloadClientItem(
    downloadClient: DownloadClient,
    connectionState: DownloadClientConnectionState,
    onTestConnection: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onEdit
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = downloadClient.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = downloadClient.type.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (connectionState) {
                    is DownloadClientConnectionState.Initial -> {
                        Icon(Icons.Default.Sync, contentDescription = null)
                        Text(mokoString(MR.strings.not_tested))
                    }

                    is DownloadClientConnectionState.Loading -> {
                        CircularProgressIndicator()
                        Text(mokoString(MR.strings.testing_connection))
                    }

                    is DownloadClientConnectionState.Success -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(mokoString(MR.strings.connected))
                    }

                    is DownloadClientConnectionState.Error -> {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = connectionState.message ?: mokoString(MR.strings.error),
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    else -> {}
                }
            }

            Button(
                onClick = onTestConnection,
                enabled = connectionState !is DownloadClientConnectionState.Loading
            ) {
                Text(mokoString(MR.strings.test_connection))
            }
        }
    }
}
