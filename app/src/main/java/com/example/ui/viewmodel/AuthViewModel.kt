package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
  val isLoading: Boolean = false,
  val errorMessage: String? = null,
  val successMessage: String? = null,
  val isRegisteredSuccessfully: Boolean = false
)

class AuthViewModel(
  private val authRepository: AuthRepository
) : ViewModel() {

  private val _uiState = MutableStateFlow(AuthUiState())
  val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

  fun login(email: String, password: String, onSuccess: () -> Unit = {}) {
    _uiState.update { it.copy(isLoading = true, errorMessage = null) }
    viewModelScope.launch {
      val result = authRepository.login(email, password)
      result.fold(
        onSuccess = {
          _uiState.update { it.copy(isLoading = false, errorMessage = null) }
          onSuccess()
        },
        onFailure = { error ->
          _uiState.update {
            it.copy(
              isLoading = false,
              errorMessage = error.localizedMessage ?: "Login gagal. Silakan coba lagi."
            )
          }
        }
      )
    }
  }

  fun register(
    name: String,
    email: String,
    password: String,
    confirmPassword: String,
    onSuccess: () -> Unit = {}
  ) {
    _uiState.update { it.copy(isLoading = true, errorMessage = null) }
    viewModelScope.launch {
      val result = authRepository.register(name, email, password, confirmPassword)
      result.fold(
        onSuccess = {
          _uiState.update {
            it.copy(
              isLoading = false,
              errorMessage = null,
              successMessage = "Akun berhasil dibuat! Silakan login.",
              isRegisteredSuccessfully = true
            )
          }
          onSuccess()
        },
        onFailure = { error ->
          _uiState.update {
            it.copy(
              isLoading = false,
              errorMessage = error.localizedMessage ?: "Registrasi gagal."
            )
          }
        }
      )
    }
  }

  fun clearMessages() {
    _uiState.update { it.copy(errorMessage = null, successMessage = null, isRegisteredSuccessfully = false) }
  }

  companion object {
    fun provideFactory(authRepository: AuthRepository): ViewModelProvider.Factory =
      object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
          return AuthViewModel(authRepository) as T
        }
      }
  }
}
