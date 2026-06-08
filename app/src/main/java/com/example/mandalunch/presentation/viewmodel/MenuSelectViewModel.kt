package com.example.mandalunch.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mandalunch.domain.model.Category
import com.example.mandalunch.domain.model.Menu
import com.example.mandalunch.domain.model.RecommendHistory
import com.example.mandalunch.domain.repository.MenuRepository
import com.example.mandalunch.domain.usecase.GetBoardMenusByCategoryUseCase
import com.example.mandalunch.domain.usecase.GetCategoriesUseCase
import com.example.mandalunch.domain.usecase.SaveHistoryUseCase
import com.example.mandalunch.domain.usecase.ToggleFavoriteUseCase
import com.example.mandalunch.presentation.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MenuSelectUiState(
    val category: Category? = null,
    val menus: List<Menu> = emptyList(),
    val spinState: MenuSpinState = MenuSpinState.Idle,
    val highlightedIndex: Int = -1   // menus 리스트의 인덱스 (0..7)
)

sealed class MenuSelectUiEvent {
    data class NavigateToResult(val categoryId: Int, val menuName: String) : MenuSelectUiEvent()
}

@HiltViewModel
class MenuSelectViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getBoardMenusByCategoryUseCase: GetBoardMenusByCategoryUseCase,
    private val saveHistoryUseCase: SaveHistoryUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val menuRepository: MenuRepository
) : ViewModel() {

    private val categoryId: Int =
        savedStateHandle.get<Int>(Routes.ARG_CATEGORY_ID) ?: 1

    private val _uiState = MutableStateFlow(MenuSelectUiState())
    val uiState: StateFlow<MenuSelectUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<MenuSelectUiEvent>()
    val events: SharedFlow<MenuSelectUiEvent> = _events.asSharedFlow()

    /**
     * 테스트 시 결정성을 부여하기 위한 random provider.
     * 프로덕션 기본값은 `(0..7).random()`이며, 테스트에서는 인스턴스 생성 직후 교체한다.
     */
    internal var randomProvider: () -> Int = { (0..7).random() }

    init {
        loadCategory()
        observeMenus()
    }

    private fun loadCategory() {
        viewModelScope.launch {
            val categories = getCategoriesUseCase().first()
            val cat = categories.firstOrNull { it.id == categoryId }
            _uiState.update { it.copy(category = cat) }
        }
    }

    private fun observeMenus() {
        viewModelScope.launch {
            getBoardMenusByCategoryUseCase(categoryId).collect { list ->
                // 즐겨찾기 우선 정렬 후 id 오름차순 (안정 정렬)
                val sorted = list.sortedWith(
                    compareByDescending<Menu> { it.isFavorite }.thenBy { it.id }
                )
                _uiState.update { it.copy(menus = sorted) }
            }
        }
    }

    /**
     * 메뉴의 즐겨찾기 상태를 토글한다.
     * 스핀 중(Spinning/Selected)에는 호출되지 않아야 하며, UI 측 가드와 함께 동작한다.
     */
    fun onToggleFavorite(menu: Menu) {
        if (_uiState.value.spinState !is MenuSpinState.Idle) return
        viewModelScope.launch {
            toggleFavoriteUseCase(menu.id, !menu.isFavorite)
        }
    }

    fun onSpinClick() {
        val menus = _uiState.value.menus
        if (menus.size < 8) return
        if (_uiState.value.spinState is MenuSpinState.Spinning) return

        val targetIndex = randomProvider()
        _uiState.update { it.copy(spinState = MenuSpinState.Spinning, highlightedIndex = -1) }

        viewModelScope.launch {
            runSpinAnimation(
                targetIndex = targetIndex,
                onHighlight = { pos -> _uiState.update { it.copy(highlightedIndex = pos) } },
                onComplete = { finalIdx ->
                    _uiState.update {
                        it.copy(
                            spinState = MenuSpinState.Selected(finalIdx),
                            highlightedIndex = finalIdx
                        )
                    }
                }
            )
        }
    }

    fun onGoToResult() {
        val state = _uiState.value
        val selected = state.spinState as? MenuSpinState.Selected ?: return
        val menu = state.menus.getOrNull(selected.menuIndex) ?: return
        val category = state.category ?: return

        viewModelScope.launch {
            saveAndMark(menu)
            _events.emit(MenuSelectUiEvent.NavigateToResult(category.id, menu.name))
        }
    }

    fun resetSpin() {
        _uiState.update { it.copy(spinState = MenuSpinState.Idle, highlightedIndex = -1) }
    }

    private suspend fun saveAndMark(menu: Menu) {
        val now = System.currentTimeMillis()
        val categoryName = _uiState.value.category?.name ?: ""
        menuRepository.updateLastRecommended(menu.id, now)
        saveHistoryUseCase(
            RecommendHistory(
                menuId = menu.id,
                menuName = menu.name,
                categoryName = categoryName,
                recommendedAt = now
            )
        )
    }

    private suspend fun runSpinAnimation(
        targetIndex: Int,
        onHighlight: (Int) -> Unit,
        onComplete: (Int) -> Unit
    ) {
        val rounds = 3
        val totalSteps = rounds * 8 + targetIndex + 1
        for (step in 0 until totalSteps) {
            onHighlight(step % 8)
            delay(getSpinDelay(step, totalSteps))
        }
        onComplete(targetIndex)
    }

    private fun getSpinDelay(step: Int, total: Int): Long {
        val ratio = step.toFloat() / total
        return when {
            ratio < 0.50f -> 70L
            ratio < 0.72f -> 120L
            ratio < 0.88f -> 200L
            ratio < 0.96f -> 320L
            else          -> 480L
        }
    }
}
