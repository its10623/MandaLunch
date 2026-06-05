package com.example.mandalunch.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mandalunch.domain.model.Category
import com.example.mandalunch.domain.model.Menu
import com.example.mandalunch.domain.usecase.ClearAllFavoritesUseCase
import com.example.mandalunch.domain.usecase.GetCategoriesUseCase
import com.example.mandalunch.domain.usecase.GetFavoritesUseCase
import com.example.mandalunch.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavoriteCategorySection(
    val categoryId: Int,
    val categoryName: String,
    val categoryEmoji: String,
    val categoryPosition: Int,
    val menus: List<Menu>
)

data class FavoriteUiState(
    val sections: List<FavoriteCategorySection> = emptyList(),
    val isLoading: Boolean = true,
    val isEmpty: Boolean = false,
    val showClearConfirm: Boolean = false
)

sealed class FavoriteUiEvent {
    object NavigateBack : FavoriteUiEvent()
    object ClearAllCompleted : FavoriteUiEvent()
}

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val clearAllFavoritesUseCase: ClearAllFavoritesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoriteUiState())
    val uiState: StateFlow<FavoriteUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<FavoriteUiEvent>()
    val events: SharedFlow<FavoriteUiEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            combine(
                getFavoritesUseCase(),
                getCategoriesUseCase()
            ) { favorites, categories ->
                groupByCategory(favorites, categories)
            }.collect { sections ->
                _uiState.update {
                    it.copy(
                        sections = sections,
                        isLoading = false,
                        isEmpty = sections.isEmpty()
                    )
                }
            }
        }
    }

    fun onBackClick() {
        viewModelScope.launch { _events.emit(FavoriteUiEvent.NavigateBack) }
    }

    fun onToggleFavorite(menu: Menu) {
        viewModelScope.launch {
            toggleFavoriteUseCase(menu.id, !menu.isFavorite)
        }
    }

    fun onClearAllClick() {
        _uiState.update { it.copy(showClearConfirm = true) }
    }

    fun onDismissClearConfirm() {
        _uiState.update { it.copy(showClearConfirm = false) }
    }

    fun onConfirmClearAll() {
        viewModelScope.launch {
            clearAllFavoritesUseCase()
            _uiState.update { it.copy(showClearConfirm = false) }
            _events.emit(FavoriteUiEvent.ClearAllCompleted)
        }
    }

    private fun groupByCategory(
        favorites: List<Menu>,
        categories: List<Category>
    ): List<FavoriteCategorySection> {
        if (favorites.isEmpty()) return emptyList()
        val byId = categories.associateBy { it.id }
        return favorites
            .groupBy { it.categoryId }
            .mapNotNull { (catId, menus) ->
                val cat = byId[catId] ?: return@mapNotNull null
                FavoriteCategorySection(
                    categoryId = cat.id,
                    categoryName = cat.name,
                    categoryEmoji = cat.emoji,
                    categoryPosition = cat.position,
                    menus = menus.sortedBy { it.name }
                )
            }
            .sortedBy { it.categoryPosition }
    }
}
