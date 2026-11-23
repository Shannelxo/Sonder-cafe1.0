package com.sondercafe.app.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sondercafe.app.core.result.AppResult
import com.sondercafe.app.domain.model.Order
import com.sondercafe.app.domain.repository.AuthRepository
import com.sondercafe.app.domain.repository.OrderRepository
import com.sondercafe.app.domain.service.OrderNotifier
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminConsoleUiState(
    val isLoading: Boolean = true,
    /** False for anyone who isn't an admin; the screen then shows nothing but a message. */
    val isAuthorised: Boolean = false,
    val activeOrders: List<Order> = emptyList(),
    val collectedOrders: List<Order> = emptyList(),
    /** Orders with a status change in progress, so their button can be disabled. */
    val updatingOrderIds: Set<Long> = emptySet(),
    val errorMessage: String? = null
)

private data class AdminOrders(val isAuthorised: Boolean, val orders: List<Order>, val errorMessage: String? = null)

private data class AdminActions(val updatingOrderIds: Set<Long> = emptySet(), val errorMessage: String? = null)

/**
 * Staff view of every order, with a button to move each one to its next status. Navigation
 * already hides this screen from customers; the role is checked again here and before every
 * change, so reaching the screen some other way still doesn't allow anything.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AdminConsoleViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val authRepository: AuthRepository,
    private val orderNotifier: OrderNotifier
) : ViewModel() {

    private val actions = MutableStateFlow(AdminActions())

    private val orders = authRepository.currentUser.flatMapLatest { user ->
        if (user?.isAdmin != true) {
            flowOf(AdminOrders(isAuthorised = false, orders = emptyList()))
        } else {
            orderRepository.observeAllOrders()
                .map { AdminOrders(isAuthorised = true, orders = it) }
                .catch { emit(AdminOrders(isAuthorised = true, orders = emptyList(), errorMessage = "Couldn't load orders")) }
        }
    }

    val uiState: StateFlow<AdminConsoleUiState> = combine(orders, actions) { o, a ->
        val (active, collected) = o.orders.partition { it.status.isActive }
        AdminConsoleUiState(
            isLoading = false,
            isAuthorised = o.isAuthorised,
            // Oldest first, so staff work through the queue in the order it arrived.
            activeOrders = active.sortedBy { it.createdAt },
            collectedOrders = collected,
            updatingOrderIds = a.updatingOrderIds,
            errorMessage = a.errorMessage ?: o.errorMessage
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AdminConsoleUiState())

    fun advance(order: Order) {
        val next = order.status.next() ?: return
        if (order.id in actions.value.updatingOrderIds) return
        actions.update { it.copy(updatingOrderIds = it.updatingOrderIds + order.id, errorMessage = null) }

        viewModelScope.launch {
            val user = authRepository.currentUser.first()
            val result = if (user?.isAdmin != true) {
                AppResult.Error("Only café staff can update orders")
            } else {
                // The repository only accepts the move if the order is still at the status we
                // saw. If the simulation moved it first, this fails cleanly instead of skipping
                // a step.
                orderRepository.updateStatus(order.id, next)
            }
            if (result is AppResult.Success) {
                orderNotifier.notifyStatusChanged(order.id, result.data.status)
            }
            actions.update {
                it.copy(
                    updatingOrderIds = it.updatingOrderIds - order.id,
                    errorMessage = (result as? AppResult.Error)?.message
                )
            }
        }
    }

    fun dismissError() = actions.update { it.copy(errorMessage = null) }
}
