package com.sondercafe.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sondercafe.app.core.result.AppResult
import com.sondercafe.app.core.validation.InputSanitizer
import com.sondercafe.app.core.validation.Validators
import com.sondercafe.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.update {
            it.copy(email = value.take(Validators.EMAIL_MAX_LENGTH), emailError = null, errorMessage = null)
        }
    }

    fun onPasswordChange(value: String) {
        _uiState.update {
            it.copy(password = value.take(Validators.PASSWORD_MAX_LENGTH), passwordError = null, errorMessage = null)
        }
    }

    fun submit() {
        val state = _uiState.value
        if (state.isLoading) return

        val email = InputSanitizer.email(state.email)
        // Only check the password is there. Strength rules belong to registration; applying
        // them here would tell an attacker something about the rules for free.
        val emailError = Validators.email(email)
        val passwordError = if (state.password.isEmpty()) "Enter your password" else null
        if (emailError != null || passwordError != null) {
            _uiState.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = authRepository.login(email, state.password)) {
                is AppResult.Success -> _uiState.update {
                    it.copy(isLoading = false, password = "", isLoggedIn = true)
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }
}
