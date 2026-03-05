package com.dnfapps.arrmatey.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.downloadclient.database.DownloadClientConflictField
import com.dnfapps.arrmatey.downloadclient.model.DownloadClient
import com.dnfapps.arrmatey.downloadclient.model.DownloadClientType
import com.dnfapps.arrmatey.downloadclient.state.DownloadClientMutationState
import com.dnfapps.arrmatey.downloadclient.viewmodel.DownloadClientSettingsViewModel
import com.dnfapps.arrmatey.navigation.Navigation
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.navigation.SettingsScreen
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.DropdownPicker
import com.dnfapps.arrmatey.ui.components.LabelledSwitch
import com.dnfapps.arrmatey.ui.components.navigation.BackButton
import com.dnfapps.arrmatey.utils.mokoString
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditDownloadClientScreen(
    clientId: Long? = null,
    viewModel: DownloadClientSettingsViewModel = koinInject(),
    navigationManager: NavigationManager = koinInject(),
    navigation: Navigation<SettingsScreen> = navigationManager.settings()
) {
    val downloadClients by viewModel.downloadClients.collectAsStateWithLifecycle()
    val mutationState by viewModel.mutationState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }

    val existingClient = clientId?.let { id -> downloadClients.find { it.id == id } }
    val isEditing = existingClient != null

    var label by remember(existingClient) { mutableStateOf(existingClient?.label ?: "") }
    var selectedType by remember(existingClient) { mutableStateOf(existingClient?.type ?: DownloadClientType.QBittorrent) }
    var url by remember(existingClient) { mutableStateOf(existingClient?.url ?: "") }
    var username by remember(existingClient) { mutableStateOf(existingClient?.username ?: "") }
    var password by remember(existingClient) { mutableStateOf(existingClient?.password ?: "") }
    var apiKey by remember(existingClient) { mutableStateOf(existingClient?.apiKey ?: "") }
    var enabled by remember(existingClient) { mutableStateOf(existingClient?.enabled ?: true) }

    var conflictFields by remember { mutableStateOf<List<DownloadClientConflictField>>(emptyList()) }

    LaunchedEffect(Unit) {
        viewModel.resetMutationState()
    }

    LaunchedEffect(mutationState) {
        when (val state = mutationState) {
            is DownloadClientMutationState.Success -> {
                viewModel.resetMutationState()
                navigation.popBackStack()
            }
            is DownloadClientMutationState.Conflict -> {
                conflictFields = state.fields
            }
            is DownloadClientMutationState.Error -> {
                snackbarHostState.showSnackbar(state.message)
            }
            else -> {
                conflictFields = emptyList()
            }
        }
    }

    val titleText = if (isEditing) mokoString(MR.strings.edit_download_client) else mokoString(MR.strings.add_download_client)

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(text = titleText) },
                navigationIcon = {
                    BackButton(navigation)
                },
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text(mokoString(MR.strings.client_label)) },
                isError = conflictFields.contains(DownloadClientConflictField.DownloadClientLabel),
                supportingText = if (conflictFields.contains(DownloadClientConflictField.DownloadClientLabel)) {
                    { Text(mokoString(MR.strings.field_conflict, mokoString(MR.strings.client_label)), color = MaterialTheme.colorScheme.error) }
                } else null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            DropdownPicker(
                options = DownloadClientType.entries,
                selectedOption = selectedType,
                onOptionSelected = { selectedType = it },
                getOptionLabel = { it.displayName },
                label = { Text(mokoString(MR.strings.client_type)) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text(mokoString(MR.strings.client_url)) },
                isError = conflictFields.contains(DownloadClientConflictField.DownloadClientUrl),
                supportingText = if (conflictFields.contains(DownloadClientConflictField.DownloadClientUrl)) {
                    { Text(mokoString(MR.strings.field_conflict, mokoString(MR.strings.client_url)), color = MaterialTheme.colorScheme.error) }
                } else null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text(mokoString(MR.strings.client_username)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(mokoString(MR.strings.client_password)) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                label = { Text(mokoString(MR.strings.client_api_key)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            LabelledSwitch(
                label = mokoString(MR.strings.client_enabled),
                checked = enabled,
                onCheckedChange = { enabled = it }
            )

            FilledTonalButton(
                onClick = {
                    val client = DownloadClient(
                        id = existingClient?.id ?: 0,
                        type = selectedType,
                        label = label,
                        url = url,
                        username = username,
                        password = password,
                        apiKey = apiKey,
                        enabled = enabled,
                        selected = existingClient?.selected ?: false
                    )
                    if (isEditing) {
                        viewModel.updateClient(client)
                    } else {
                        viewModel.createClient(client)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text(mokoString(MR.strings.save))
            }
        }
    }
}
