package com.sondercafe.app.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sondercafe.app.core.result.AppResult
import com.sondercafe.app.domain.model.CartLine
import com.sondercafe.app.domain.model.itemCount
import com.sondercafe.app.domain.model.totalPence
import com.sondercafe.app.domain.repository.AuthRepository
import com.sondercafe.app.domain.repository.CartRepository
import com.sondercafe.app.domain.usecase.PlaceOrderUseCase
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

data class CartUiState(
    val lines: List<CartLine> = emptyList(),
    val isLoading: Boolean = true,
    val isPlacingOrder: Boolean = false,
    val errorMessage: String? = null,
    /** Set once an order goes through; the screen navigates and then calls [CartViewModel.onOrderPlacedHandled]. */
    val placedOrderId: Long? = null
) {
    val totalPence: Int get() = lines.totalPence()
    val itemCount: Int get() = lines.itemCount()
    val hasUnavailableItems: Boolean get() = lines.any { !it.menuItem.isAvailable }
    val canPlaceOrder: Boolean get() = lines.isNotEmpty() && !hasUnavailableItems && !isPlacingOrder
}

private data class CartActions(
    val isPlacingOrder: Boolean = false,
    val errorMessage: String? = null,
    val placedOrderId: Long? = null
)

private data class CartLoad(val lines: List<CartLine>, val errorMessage: String? = null)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val authRepository: AuthRepository,
    private val placeOrderUseCase: PlaceOrderUseCase
) : ViewModel() {

    private val actions = MutableStateFlow(CartActions())

    private val cart = authRepository.currentUser.flatMapLatest { user ->
        if (user == null) flowOf(CartLoad(emptyList()))
        else cartRepository.observeCart(user.id).map { CartLoad(it) }
    }.catch { emit(CartLoad(emptyList(), "Couldn't load your cart. Please try again.")) }

    val uiState: StateFlow<CartUiState> = combine(cart, actions) { load, a ->
        CartUiState(
            lines = load.lines,
            isLoading = false,
            isPlacingOrder = a.isPlacingOrder,
            errorMessage = a.errorMessage ?: load.errorMessage,
            placedOrderId = a.placedOrderId
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CartUiState())

    fun changeQuantity(menuItemId: Long, quantity: Int) = runForUser { userId ->
        cartRepository.updateQuantity(userId, menuItemId, quantity)
    }

    fun removeItem(menuItemId: Long) = runForUser { userId ->
        cartRepository.removeItem(userId, menuItemId)
    }

    fun placeOrder() {
        if (actions.value.isPlacingOrder) return
        actions.update { it.copy(isPlacingOrder = true, errorMessage = null) }
        viewModelScope.launch {
            val user = authRepository.currentUser.first()
            if (user == null) {
                actions.update { it.copy(isPlacingOrder = false, errorMessage = NOT_LOGGED_IN) }
                return@launch
            }
            when (val result = placeOrderUseCase(user.id)) {
                is AppResult.Success -> actions.update { CartActions(placedOrderId = result.data) }
                is AppResult.Error -> actions.update { it.copy(isPlacingOrder = false, errorMessage = result.message) }
            }
        }
    }

    fun onOrderPlacedHandled() = actions.update { it.copy(placedOrderId = null) }

    fun dismissError() = actions.update { it.copy(errorMessage = null) }

    private fun runForUser(block: suspend (userId: Long) -> AppResult<Unit>) {
        actions.update { it.copy(errorMessage = null) }
        viewModelScope.launch {
            val user = authRepository.currentUser.first()
            val result = if (user == null) AppResult.Error(NOT_LOGGED_IN) else block(user.id)
            if (result is AppResult.Error) actions.update { it.copy(errorMessage = result.message) }
        }
    }

    private companion object {
        const val NOT_LOGGED_IN = "You're not logged in"
    }
}
