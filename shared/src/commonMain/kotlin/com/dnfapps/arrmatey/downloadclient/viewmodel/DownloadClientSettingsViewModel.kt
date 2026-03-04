package com.dnfapps.arrmatey.downloadclient.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.downloadclient.model.DownloadClient
import com.dnfapps.arrmatey.downloadclient.state.DownloadClientConnectionState
import com.dnfapps.arrmatey.downloadclient.state.DownloadClientMutationState
import com.dnfapps.arrmatey.downloadclient.usecase.DeleteDownloadClientUseCase
import com.dnfapps.arrmatey.downloadclient.usecase.ObserveDownloadClientsUseCase
import com.dnfapps.arrmatey.downloadclient.usecase.TestDownloadClientConnectionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DownloadClientSettingsViewModel(
    observeDownloadClientsUseCase: ObserveDownloadClientsUseCase,
    private val testDownloadClientConnectionUseCase: TestDownloadClientConnectionUseCase,
    private val deleteDownloadClientUseCase: DeleteDownloadClientUseCase
): ViewModel() {

    val downloadClients = observeDownloadClientsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _connectionStates = MutableStateFlow<Map<Long, DownloadClientConnectionState>>(emptyMap())
    val connectionStates: StateFlow<Map<Long, DownloadClientConnectionState>> = _connectionStates.asStateFlow()

    private val _mutationState = MutableStateFlow<DownloadClientMutationState>(DownloadClientMutationState.Initial)
    val mutationState: StateFlow<DownloadClientMutationState> = _mutationState.asStateFlow()

    fun testConnection(id: Long) {
        viewModelScope.launch {
            testDownloadClientConnectionUseCase(id).collect { state ->
                _connectionStates.value = _connectionStates.value.toMutableMap().apply {
                    put(id, state)
                }
            }
        }
    }

    fun deleteClient(downloadClient: DownloadClient) {
        viewModelScope.launch {
            runCatching {
                deleteDownloadClientUseCase(downloadClient)
            }.onSuccess {
                _mutationState.value = DownloadClientMutationState.Success(downloadClient.id)
            }.onFailure { error ->
                _mutationState.value = DownloadClientMutationState.Error(
                    error.message ?: "Failed to delete"
                )
            }
        }
    }

    fun resetMutationState() {
        _mutationState.value = DownloadClientMutationState.Initial
    }
}
