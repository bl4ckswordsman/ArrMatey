package com.dnfapps.arrmatey.arr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.arr.state.ArrDashboardState
import com.dnfapps.arrmatey.client.ErrorType
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.repository.InstanceScopedRepository
import com.dnfapps.arrmatey.instances.usecase.GetInstanceRepositoryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class ArrInstanceDashboardViewModel(
    private val instanceId: Long,
    private val getInstanceRepositoryUseCase: GetInstanceRepositoryUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<ArrDashboardState>(ArrDashboardState.Initial)
    val state: StateFlow<ArrDashboardState> = _state.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _instance = MutableStateFlow<Instance?>(null)
    val instance: StateFlow<Instance?> = _instance.asStateFlow()

    private var repository: InstanceScopedRepository? = null

    init {
        loadInstanceAndObserve()
    }

    private fun loadInstanceAndObserve() {
        viewModelScope.launch {
            _state.value = ArrDashboardState.Loading

            repository = getInstanceRepositoryUseCase(instanceId)
            val currentRepo = repository
            if (currentRepo == null) {
                _state.value = ArrDashboardState.Error(
                    type = ErrorType.Unexpected,
                    message = "Could not connect to instance repository"
                )
                return@launch
            }
            _instance.value = currentRepo.instance

            // Trigger initial refresh
            refresh()

            // Combine data flows AND the refresh flow
            combine(
                currentRepo.softwareStatus,
                currentRepo.diskSpace,
                currentRepo.health,
                _isRefreshing
            ) { software, disks, health, refreshing ->
                ArrDashboardState.Success(
                    softwareStatus = software,
                    disks = disks,
                    healthItems = health,
                    isRefreshing = refreshing
                )
            }.collect { newState ->
                _state.value = newState
            }
        }
    }

    fun refresh() {
        // Prevent multiple simultaneous refreshes
        if (_isRefreshing.value) return

        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                repository?.refreshInstanceStatuses()
            } catch (e: Exception) {
                // If we are in Success state, maybe show a toast instead of changing state
                if (_state.value is ArrDashboardState.Initial) {
                    _state.value = ArrDashboardState.Error(ErrorType.Unexpected, e.message)
                }
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}