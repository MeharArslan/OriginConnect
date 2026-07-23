package com.meharenterprises.originconnect.ui.auth
import androidx.lifecycle.*
import com.meharenterprises.originconnect.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class OtpSent(val code: String?) : AuthState()
    data class Success(val isNewUser: Boolean) : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(private val repo: AuthRepository) : ViewModel() {
    private val _state = MutableLiveData<AuthState>(AuthState.Idle)
    val state: LiveData<AuthState> = _state

    fun sendOtp(phone: String) {
        _state.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val res = repo.sendOtp(phone)
                if (res.isSuccessful) _state.value = AuthState.OtpSent(res.body()?.code)
                else _state.value = AuthState.Error("Failed to send OTP")
            } catch (e: Exception) {
                _state.value = AuthState.Error(e.message ?: "Network error")
            }
        }
    }

    fun verifyOtp(phone: String, code: String) {
        _state.value = AuthState.Loading
        viewModelScope.launch {
            val result = repo.verifyOtp(phone, code)
            result.fold(
                onSuccess = { _state.value = AuthState.Success(it == "new") },
                onFailure = { _state.value = AuthState.Error(it.message ?: "Verification failed") }
            )
        }
    }
}
