package com.example.refac_driverapp.feature.register


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.refac_driverapp.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(private val repository: UserRepository) : ViewModel() {

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> get() = _registerState

    fun register(email: String, password: String, name: String) {
        if (email.isBlank() || password.length < 8) {
            _registerState.value = RegisterState.Error("유효한 이메일과 8자 이상의 비밀번호를 입력해주세요.")
            return
        }

        _registerState.value = RegisterState.Loading

        viewModelScope.launch {
            val result = repository.register(email, password, name)
            _registerState.value = if (result.isSuccess) {
                RegisterState.Success
            } else {
                RegisterState.Error(result.exceptionOrNull()?.message ?: "알 수 없는 오류")
            }
        }
    }

    sealed class RegisterState {
        object Idle : RegisterState()
        object Loading : RegisterState()
        object Success : RegisterState()
        data class Error(val message: String) : RegisterState()
    }
}
