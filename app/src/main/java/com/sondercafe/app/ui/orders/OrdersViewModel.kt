package com.sondercafe.app.ui.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sondercafe.app.domain.model.Order
import com.sondercafe.app.domain.repository.AuthRepository
import com.sondercafe.app.domain.repository.OrderRepository
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

data class OrdersUiState(
    val activeOrders: List<Order> = emptyList(),
    val pastOrders: List<Order> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
) {
    val isEmpty: Boolean get() = activeOrders.isEmpty() && pastOrders.isEmpty()
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class OrdersViewModel @Inject constructor(
    orderRepository: OrderRepository,
    authRepository: AuthRepository
) : ViewModel() {

    val uiState: StateFlow<OrdersUiState> = authRepository.currentUser
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList()) else orderRepository.observeOrders(user.id)
        }
        .map { orders ->
            // Orders come newest first; partition keeps that order inside each group.
            val (active, past) = orders.partition { it.status.isActive }
            OrdersUiState(activeOrders = active, pastOrders = past, isLoading = false)
        }
        .catch { emit(OrdersUiState(isLoading = false, errorMessage = "Couldn't load your orders")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OrdersUiState())
}
