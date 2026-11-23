package com.sondercafe.app.ui.menu

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sondercafe.app.core.result.AppResult
import com.sondercafe.app.core.validation.Validators
import com.sondercafe.app.domain.model.MenuItem
import com.sondercafe.app.domain.repository.AuthRepository
import com.sondercafe.app.domain.repository.CartRepository
import com.sondercafe.app.domain.repository.MenuRepository
import com.sondercafe.app.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MenuItemDetailUiState(
    val item: MenuItem? = null,
    val isLoading: Boolean = true,
    val notFound: Boolean = false,
    val quantity: Int = Validators.MIN_QUANTITY,
    val isAdding: Boolean = false,
    val addedMessage: String? = null,
    val errorMessage: String? = null
)

private data class LocalState(
    val quantity: Int = Validators.MIN_QUANTITY,
    val isAdding: Boolean = false,
    val addedMessage: String? = null,
    val errorMessage: String? = null
)

// Until the first value arrives, stateIn's initial state (isLoading = true) is shown.
private sealed interface ItemLoad {
    data class Loaded(val item: MenuItem?) : ItemLoad
    data object Failed : ItemLoad
}

@HiltViewModel
class MenuItemDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    menuRepository: MenuRepository,
    private val cartRepository: CartRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val itemId: Long = checkNotNull(savedStateHandle.get<Long>(Routes.ARG_ITEM_ID)) { "Missing item ID" }

    private val local = MutableStateFlow(LocalState())

    private val item = menuRepository.observeMenuItem(itemId)
        .map<MenuItem?, ItemLoad> { ItemLoad.Loaded(it) }
        .catch { emit(ItemLoad.Failed) }

    val uiState: StateFlow<MenuItemDetailUiState> = combine(item, local) { load, l ->
        MenuItemDetailUiState(
            item = (load as? ItemLoad.Loaded)?.item,
            isLoading = false,
            // The item could also disappear while the screen is open; treat that the same way.
            notFound = load is ItemLoad.Loaded && load.item == null,
            quantity = l.quantity,
            isAdding = l.isAdding,
            addedMessage = l.addedMessage,
            errorMessage = l.errorMessage ?: if (load is ItemLoad.Failed) "Couldn't load this item" else null
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MenuItemDetailUiState())

    fun onQuantityChange(quantity: Int) {
        val clamped = quantity.coerceIn(Validators.MIN_QUANTITY, Validators.MAX_QUANTITY)
        local.update { it.copy(quantity = clamped, addedMessage = null, errorMessage = null) }
    }

    fun addToCart() {
        val state = local.value
        if (state.isAdding) return
        local.update { it.copy(isAdding = true, addedMessage = null, errorMessage = null) }

        viewModelScope.launch {
            val user = authRepository.currentUser.first()
            if (user == null) {
                local.update { it.copy(isAdding = false, errorMessage = "Log in to add items to your cart") }
                return@launch
            }
            val name = uiState.value.item?.name ?: "Item"
            when (val result = cartRepository.addItem(user.id, itemId, state.quantity)) {
                is AppResult.Success -> local.update {
                    LocalState(addedMessage = "Added ${state.quantity} × $name to your cart")
                }
                is AppResult.Error -> local.update { it.copy(isAdding = false, errorMessage = result.message) }
            }
        }
    }
}
