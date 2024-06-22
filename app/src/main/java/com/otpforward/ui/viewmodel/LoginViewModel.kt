package com.otpforward.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.otpforward.data.model.BaseResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import com.otpforward.data.model.Login
import com.otpforward.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    sealed interface LoginUiState {
        data object Empty : LoginUiState
        data object Loading : LoginUiState
        data class Success(val response: BaseResponse<Login>?) : LoginUiState
        data class Error(val message: String) : LoginUiState
    }

    private val _loginState = MutableStateFlow<LoginUiState>(LoginUiState.Empty)
    val loginState: StateFlow<LoginUiState> = _loginState

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginUiState.Loading
            try {
                val response = userRepository.login(username, password)
                if (response?.status == true) {
                    _loginState.value = LoginUiState.Success(response)
                } else {
                    response?.msg?.let {
                        _loginState.value = LoginUiState.Error(it)
                    } ?: run {
                        _loginState.value = LoginUiState.Error("Something went to wrong")
                    }
                }
            } catch (e: Exception) {
                _loginState.value = LoginUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}