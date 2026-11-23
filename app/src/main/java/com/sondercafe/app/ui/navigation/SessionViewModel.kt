package com.sondercafe.app.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sondercafe.app.domain.model.User
import com.sondercafe.app.domain.model.itemCount
import com.sondercafe.app.domain.repository.AuthRepository
import com.sondercafe.app.domain.repository.CartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface SessionState {
    /** Still reading the saved session; nothing should be shown yet. */
    data object Loading : SessionState
    data object LoggedOut : SessionState
    data class LoggedIn(val user: User) : SessionState
}

/** App-wide login state, plus the cart count for the bottom bar badge. */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SessionViewModel @Inject constructor(
    authRepository: AuthRepository,
    cartRepository: CartRepository
) : ViewModel() {

    // Eagerly, so the session is read while the first frame is drawn rather than after.
    val session: StateFlow<SessionState> = authRepository.currentUser
        .map { user -> if (user == null) SessionState.LoggedOut else SessionState.LoggedIn(user) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SessionState.Loading)

    val cartItemCount: StateFlow<Int> = authRepository.currentUser
        .flatMapLatest { user ->
            if (user == null) flowOf(0) else cartRepository.observeCart(user.id).map { it.itemCount() }
        }
        .catch { emit(0) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
}
