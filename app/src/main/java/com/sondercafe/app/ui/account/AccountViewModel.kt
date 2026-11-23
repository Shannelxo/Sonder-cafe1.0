package com.sondercafe.app.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sondercafe.app.core.result.AppResult
import com.sondercafe.app.domain.model.User
import com.sondercafe.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AccountUiState(
    val user: User? = null,
    val showDeleteConfirmation: Boolean = false,
    val isWorking: Boolean = false,
    val errorMessage: String? = null
)

private data class AccountActions(
    val showDeleteConfirmation: Boolean = false,
    val isWorking: Boolean = false,
    val errorMessage: String? = null
)

/**
 * Logout and account deletion. Neither navigates by itself: once the session is cleared, the
 * navigation host sees there's no user and returns to the login screen.
 */
@HiltViewModel
class AccountViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val actions = MutableStateFlow(AccountActions())

    val uiState: StateFlow<AccountUiState> = combine(authRepository.currentUser, actions) { user, a ->
        AccountUiState(
            user = user,
            showDeleteConfirmation = a.showDeleteConfirmation,
            isWorking = a.isWorking,
            errorMessage = a.errorMessage
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AccountUiState())

    fun logout() {
        if (actions.value.isWorking) return
        actions.update { it.copy(isWorking = true) }
        viewModelScope.launch {
            authRepository.logout()
            actions.value = AccountActions()
        }
    }

    fun requestDelete() = actions.update { it.copy(showDeleteConfirmation = true, errorMessage = null) }

    fun cancelDelete() = actions.update { it.copy(showDeleteConfirmation = false) }

    fun confirmDelete() {
        if (actions.value.isWorking) return
        actions.update { it.copy(showDeleteConfirmation = false, isWorking = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = authRepository.deleteAccount()) {
                is AppResult.Success -> actions.value = AccountActions()
                is AppResult.Error -> actions.update { it.copy(isWorking = false, errorMessage = result.message) }
            }
        }
    }
}
