package com.sondercafe.app.ui.feedback

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sondercafe.app.core.result.AppResult
import com.sondercafe.app.core.validation.Validators
import com.sondercafe.app.domain.repository.AuthRepository
import com.sondercafe.app.domain.repository.FeedbackRepository
import com.sondercafe.app.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FeedbackUiState(
    val orderId: Long,
    /** 0 means "not chosen yet"; the user has to pick a star before sending. */
    val rating: Int = 0,
    val comment: String = "",
    val ratingError: String? = null,
    val errorMessage: String? = null,
    val isSubmitting: Boolean = false,
    val isSubmitted: Boolean = false
) {
    val commentLength: Int get() = comment.length
}

@HiltViewModel
class FeedbackViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val feedbackRepository: FeedbackRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val orderId: Long = checkNotNull(savedStateHandle.get<Long>(Routes.ARG_ORDER_ID)) { "Missing order ID" }

    private val _uiState = MutableStateFlow(FeedbackUiState(orderId = orderId))
    val uiState: StateFlow<FeedbackUiState> = _uiState.asStateFlow()

    fun onRatingChange(rating: Int) = _uiState.update {
        it.copy(rating = rating, ratingError = null, errorMessage = null)
    }

    fun onCommentChange(value: String) = _uiState.update {
        it.copy(comment = value.take(Validators.COMMENT_MAX_LENGTH), errorMessage = null)
    }

    fun submit() {
        val state = _uiState.value
        if (state.isSubmitting) return

        val ratingError = Validators.rating(state.rating)
        if (ratingError != null) {
            _uiState.update { it.copy(ratingError = ratingError) }
            return
        }

        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            val user = authRepository.currentUser.first()
            // The repository checks ownership, the order status and duplicates; this screen
            // just reports whatever it says.
            val result = if (user == null) {
                AppResult.Error("You're not logged in")
            } else {
                feedbackRepository.submitFeedback(user.id, orderId, state.rating, state.comment)
            }
            _uiState.update {
                when (result) {
                    is AppResult.Success -> it.copy(isSubmitting = false, isSubmitted = true)
                    is AppResult.Error -> it.copy(isSubmitting = false, errorMessage = result.message)
                }
            }
        }
    }
}
