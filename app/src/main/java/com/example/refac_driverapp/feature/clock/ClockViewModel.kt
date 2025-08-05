package com.example.refac_driverapp.feature.clock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.refac_driverapp.data.model.StationInfo
import com.example.refac_driverapp.data.repository.ClockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ClockViewModel(private val repository: ClockRepository) : ViewModel() {

    private val _stationList = MutableStateFlow<List<StationInfo>>(emptyList())
    val stationList: StateFlow<List<StationInfo>> get() = _stationList

    fun loadStationInfo(route: String, time: String) {
        viewModelScope.launch {
            val list = repository.fetchReservationCounts(route, time)
            _stationList.value = list
        }
    }

    fun endDrive(pushKey: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.saveEndTime(pushKey)
            onSuccess()
        }
    }
}
