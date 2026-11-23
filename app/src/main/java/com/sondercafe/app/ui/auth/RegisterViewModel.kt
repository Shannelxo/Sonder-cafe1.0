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

data class RegisterUiState(
    val displayName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val displayNameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
    val isRegistered: Boolean = false
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onDisplayNameChange(value: String) = _uiState.update {
        it.copy(displayName = value.take(Validators.NAME_MAX_LENGTH), displayNameError = null, errorMessage = null)
    }

    fun onEmailChange(value: String) = _uiState.update {
        it.copy(email = value.take(Validators.EMAIL_MAX_LENGTH), emailError = null, errorMessage = null)
    }

    fun onPasswordChange(value: String) = _uiState.update {
        it.copy(password = value.take(Validators.PASSWORD_MAX_LENGTH), passwordError = null, errorMessage = null)
    }

    fun onConfirmPasswordChange(value: String) = _uiState.update {
        it.copy(confirmPassword = value.take(Validators.PASSWORD_MAX_LENGTH), confirmPasswordError = null, errorMessage = null)
    }

    fun submit() {
        val state = _uiState.value
        if (state.isLoading) return

        val name = InputSanitizer.singleLine(state.displayName, Validators.NAME_MAX_LENGTH)
        val email = InputSanitizer.email(state.email)

        // Check every field at once so the user sees all problems together, not one at a time.
        val nameError = Validators.displayName(name)
        val emailError = Validators.email(email)
        val passwordError = Validators.password(state.password)
        val confirmError = Validators.passwordConfirmation(state.password, state.confirmPassword)

        if (listOf(nameError, emailError, passwordError, confirmError).any { it != null }) {
            _uiState.update {
                it.copy(
                    displayNameError = nameError,
                    emailError = emailError,
                    passwordError = passwordError,
                    confirmPasswordError = confirmError
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = authRepository.register(name, email, state.password)) {
                is AppResult.Success -> _uiState.update {
                    it.copy(isLoading = false, password = "", confirmPassword = "", isRegistered = true)
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }
}
