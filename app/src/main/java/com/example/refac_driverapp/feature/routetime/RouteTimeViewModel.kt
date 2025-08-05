package com.example.refac_driverapp.feature.routetime

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.refac_driverapp.data.repository.RouteTimeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*

class RouteTimeViewModel(private val repository: RouteTimeRepository) : ViewModel() {

    private val _routes = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val routes: StateFlow<Map<String, List<String>>> get() = _routes

    private val _saveResult = MutableStateFlow<Result<String>?>(null)
    val saveResult: StateFlow<Result<String>?> get() = _saveResult

    fun loadRoutes() {
        viewModelScope.launch {
            _routes.value = repository.fetchPinnedRoutes()
        }
    }

    fun getFilteredTimes(route: String): List<String> {
        val now = Calendar.getInstance()
        val nowInMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

        return _routes.value[route]
            ?.filter { time ->
                time.contains(":") && run {
                    val (h, m) = time.split(":").mapNotNull { it.toIntOrNull() }
                    h * 60 + m > nowInMinutes
                }
            }
            ?.sorted()
            ?: emptyList()
    }

    fun saveRecord(route: String, time: String) {
        viewModelScope.launch {
            _saveResult.value = repository.saveDrivedRecord(route, time)
        }
    }
}
