package com.sondercafe.app.ui.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sondercafe.app.core.result.AppResult
import com.sondercafe.app.core.validation.Validators
import com.sondercafe.app.domain.model.MenuCategory
import com.sondercafe.app.domain.model.MenuItem
import com.sondercafe.app.domain.model.Recommendation
import com.sondercafe.app.domain.model.WeatherSnapshot
import com.sondercafe.app.domain.repository.AuthRepository
import com.sondercafe.app.domain.repository.MenuRepository
import com.sondercafe.app.domain.usecase.GetRecommendationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecommendationsUiState(
    val isLoading: Boolean = true,
    val items: List<Recommendation> = emptyList(),
    /** Null when the weather couldn't be fetched; the screen says so rather than hiding it. */
    val weather: WeatherSnapshot? = null,
    val errorMessage: String? = null
)

data class MenuUiState(
    val query: String = "",
    val category: MenuCategory? = null,
    val availableOnly: Boolean = false,
    val items: List<MenuItem> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val recommendations: RecommendationsUiState = RecommendationsUiState()
) {
    val hasFilters: Boolean get() = query.isNotBlank() || category != null || availableOnly
}

private data class MenuFilters(
    val query: String = "",
    val category: MenuCategory? = null,
    val availableOnly: Boolean = false
)

private data class MenuResult(
    val items: List<MenuItem> = emptyList(),
    val errorMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MenuViewModel @Inject constructor(
    private val menuRepository: MenuRepository,
    private val authRepository: AuthRepository,
    private val getRecommendations: GetRecommendationsUseCase
) : ViewModel() {

    private val filters = MutableStateFlow(MenuFilters())
    private val recommendations = MutableStateFlow(RecommendationsUiState())

    // flatMapLatest drops the previous query's results as soon as the filters change, so a
    // slow old search can't overwrite a newer one.
    private val menuResult = filters.flatMapLatest { f ->
        menuRepository.searchMenu(f.query, f.category, f.availableOnly)
            .map { MenuResult(items = it) }
            .catch { emit(MenuResult(errorMessage = "Couldn't load the menu. Please try again.")) }
    }

    val uiState: StateFlow<MenuUiState> = combine(filters, menuResult, recommendations) { f, menu, recs ->
        MenuUiState(
            query = f.query,
            category = f.category,
            availableOnly = f.availableOnly,
            items = menu.items,
            isLoading = false,
            errorMessage = menu.errorMessage,
            recommendations = recs
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MenuUiState())

    init {
        loadRecommendations()
    }

    fun onQueryChange(value: String) = filters.update { it.copy(query = value.take(Validators.SEARCH_MAX_LENGTH)) }

    fun onCategorySelected(category: MenuCategory?) = filters.update { it.copy(category = category) }

    fun onAvailableOnlyChange(value: Boolean) = filters.update { it.copy(availableOnly = value) }

    fun loadRecommendations() {
        recommendations.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val user = authRepository.currentUser.first()
            if (user == null) {
                recommendations.value = RecommendationsUiState(isLoading = false)
                return@launch
            }
            recommendations.value = when (val result = getRecommendations(user.id)) {
                is AppResult.Success -> RecommendationsUiState(
                    isLoading = false,
                    items = result.data.recommendations,
                    weather = result.data.weather
                )
                is AppResult.Error -> RecommendationsUiState(isLoading = false, errorMessage = result.message)
            }
        }
    }
}
