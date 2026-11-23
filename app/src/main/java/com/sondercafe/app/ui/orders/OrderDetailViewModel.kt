package com.sondercafe.app.ui.orders

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sondercafe.app.domain.model.Feedback
import com.sondercafe.app.domain.model.Order
import com.sondercafe.app.domain.model.OrderStatus
import com.sondercafe.app.domain.repository.AuthRepository
import com.sondercafe.app.domain.repository.FeedbackRepository
import com.sondercafe.app.domain.repository.OrderRepository
import com.sondercafe.app.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class OrderDetailUiState(
    val order: Order? = null,
    val feedback: Feedback? = null,
    val isLoading: Boolean = true,
    val notFound: Boolean = false,
    val errorMessage: String? = null,
    val canLeaveFeedback: Boolean = false
)

/** Used by both the order detail and the order confirmation screens; they show the same order. */
@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    orderRepository: OrderRepository,
    feedbackRepository: FeedbackRepository,
    authRepository: AuthRepository
) : ViewModel() {

    private val orderId: Long = checkNotNull(savedStateHandle.get<Long>(Routes.ARG_ORDER_ID)) { "Missing order ID" }

    val uiState: StateFlow<OrderDetailUiState> = combine(
        authRepository.currentUser,
        orderRepository.observeOrder(orderId),
        feedbackRepository.observeFeedbackForOrder(orderId)
    ) { user, order, feedback ->
        // The ID comes from the route, which could come from a notification. Don't show
        // somebody else's order just because the ID was valid; admins can see any order.
        if (order == null || user == null || (order.userId != user.id && !user.isAdmin)) {
            OrderDetailUiState(isLoading = false, notFound = true)
        } else {
            OrderDetailUiState(
                order = order,
                feedback = feedback,
                isLoading = false,
                canLeaveFeedback = order.status == OrderStatus.COLLECTED &&
                    feedback == null &&
                    order.userId == user.id
            )
        }
    }
        .catch { emit(OrderDetailUiState(isLoading = false, errorMessage = "Couldn't load this order")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OrderDetailUiState())
}
