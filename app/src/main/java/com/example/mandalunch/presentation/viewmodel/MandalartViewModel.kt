package com.example.mandalunch.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mandalunch.domain.model.Category
import com.example.mandalunch.domain.model.Menu
import com.example.mandalunch.domain.usecase.GetBoardMenusByCategoryUseCase
import com.example.mandalunch.domain.usecase.GetCategoriesUseCase
import com.example.mandalunch.domain.usecase.RandomizeBoardMenusUseCase
import com.example.mandalunch.presentation.viewmodel.util.MandalaLayout
import com.example.mandalunch.presentation.viewmodel.util.SpinAnimator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MandalartUiState(
    val categories: List<Category> = emptyList(),
    val menusByCategoryId: Map<Int, List<Menu>> = emptyMap(),
    val spinState: SpinState = SpinState.Idle,
    val highlightedIndex: Int = -1   // CW_ORDER 기준 (-1 = 하이라이트 없음)
)

sealed class MandalartUiEvent {
    data class NavigateToMenuSelect(val categoryId: Int) : MandalartUiEvent()
    data class NavigateToMenuEdit(val categoryId: Int) : MandalartUiEvent()
}

@HiltViewModel
class MandalartViewModel @Inject constructor(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getBoardMenusByCategoryUseCase: GetBoardMenusByCategoryUseCase,
    private val randomizeBoardMenusUseCase: RandomizeBoardMenusUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MandalartUiState())
    val uiState: StateFlow<MandalartUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<MandalartUiEvent>()
    val events: SharedFlow<MandalartUiEvent> = _events.asSharedFlow()

    /**
     * 테스트 시 결정성을 부여하기 위한 random provider.
     * 프로덕션 기본값은 `(0..7).random()`이며, 테스트에서는 인스턴스 생성 직후 교체한다.
     */
    internal var randomProvider: () -> Int = { (0..7).random() }

    init {
        autoRandomizeBoard()
        observeCategoriesAndMenus()
    }

    /** 앱 시작 시 모든 카테고리의 보드 메뉴를 랜덤으로 교체한다. */
    private fun autoRandomizeBoard() {
        viewModelScope.launch {
            val categories = getCategoriesUseCase().first { it.isNotEmpty() }
            categories.forEach { cat ->
                try { randomizeBoardMenusUseCase(cat.id) } catch (_: Exception) {}
            }
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private fun observeCategoriesAndMenus() {
        viewModelScope.launch {
            getCategoriesUseCase()
                .flatMapLatest { categories ->
                    if (categories.isEmpty()) {
                        flowOf(categories to emptyMap<Int, List<Menu>>())
                    } else {
                        val perCategoryFlows = categories.map { cat ->
                            getBoardMenusByCategoryUseCase(cat.id)
                        }
                        combine(perCategoryFlows) { menuLists ->
                            val map = mutableMapOf<Int, List<Menu>>()
                            categories.forEachIndexed { idx, cat ->
                                map[cat.id] = menuLists[idx]
                            }
                            categories to map.toMap()
                        }
                    }
                }
                .collectLatest { (categories, menusMap) ->
                    _uiState.update {
                        it.copy(categories = categories, menusByCategoryId = menusMap)
                    }
                }
        }
    }

    /**
     * 외곽 카테고리 블록 탭 → 메뉴 편집 화면으로 진입.
     */
    fun onCategoryClick(category: Category) {
        viewModelScope.launch {
            _events.emit(MandalartUiEvent.NavigateToMenuEdit(category.id))
        }
    }

    fun onSpinClick() {
        val categories = _uiState.value.categories
        if (categories.size < 8) return
        if (_uiState.value.spinState is SpinState.Spinning) return  // 중복 클릭 방지

        val targetCwIndex = randomProvider()
        _uiState.update { it.copy(spinState = SpinState.Spinning, highlightedIndex = -1) }

        viewModelScope.launch {
            SpinAnimator.run(
                targetIndex = targetCwIndex,
                onHighlight = { cwPos ->
                    _uiState.update { it.copy(highlightedIndex = cwPos) }
                },
                onComplete = { finalCw ->
                    _uiState.update {
                        it.copy(
                            spinState = SpinState.Selected(finalCw),
                            highlightedIndex = finalCw
                        )
                    }
                }
            )
        }
    }

    fun onGoToMenuClick() {
        val state = _uiState.value
        val selected = state.spinState as? SpinState.Selected ?: return
        val category = MandalaLayout.cwIndexToCategory(selected.categoryIndex, state.categories) ?: return
        viewModelScope.launch {
            _events.emit(MandalartUiEvent.NavigateToMenuSelect(category.id))
        }
    }

    fun resetSpin() {
        _uiState.update { it.copy(spinState = SpinState.Idle, highlightedIndex = -1) }
    }

}
