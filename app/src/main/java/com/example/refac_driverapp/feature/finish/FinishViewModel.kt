package com.example.refac_driverapp.feature.finish


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.refac_driverapp.data.model.DrivedRecord
import com.example.refac_driverapp.data.repository.FinishRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FinishViewModel(private val repository: FinishRepository) : ViewModel() {
    private val _routeInfo = MutableStateFlow<DrivedRecord?>(null)
    val routeInfo: StateFlow<DrivedRecord?> get() = _routeInfo

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> get() = _error

    fun loadRouteInfo(pushKey: String) {
        viewModelScope.launch {
            val result = repository.fetchDrivedRouteInfo(pushKey)
            if (result.isSuccess) {
                _routeInfo.value = result.getOrNull()
            } else {
                _error.value = result.exceptionOrNull()?.message
            }
        }
    }
}
