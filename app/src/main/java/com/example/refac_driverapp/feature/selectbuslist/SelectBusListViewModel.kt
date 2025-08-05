package com.example.refac_driverapp.feature.selectbuslist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.refac_driverapp.data.model.DrivedRecord
import com.example.refac_driverapp.data.repository.SelectBusListRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SelectBusListViewModel(private val repository: SelectBusListRepository) : ViewModel() {

    private val _drivedList = MutableStateFlow<List<DrivedRecord>>(emptyList())
    val drivedList: StateFlow<List<DrivedRecord>> get() = _drivedList

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> get() = _error

    fun loadDrivedList() {
        viewModelScope.launch {
            val result = repository.fetchDrivedList()
            if (result.isSuccess) {
                val sorted = result.getOrNull()?.sortedByDescending {
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.date)
                } ?: emptyList()
                _drivedList.value = sorted
            } else {
                _error.value = result.exceptionOrNull()?.message
            }
        }
    }
}