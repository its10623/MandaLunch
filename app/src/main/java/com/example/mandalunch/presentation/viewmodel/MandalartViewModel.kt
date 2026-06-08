package com.example.mandalunch.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mandalunch.domain.model.Category
import com.example.mandalunch.domain.model.Menu
import com.example.mandalunch.domain.usecase.GetBoardMenusByCategoryUseCase
import com.example.mandalunch.domain.usecase.GetCategoriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
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
    private val getBoardMenusByCategoryUseCase: GetBoardMenusByCategoryUseCase
) : ViewModel() {

    companion object {
        // CW index → DB position 변환 (§0.1)
        val CW_TO_POSITION = intArrayOf(0, 1, 2, 4, 7, 6, 5, 3)
    }

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
        observeCategoriesAndMenus()
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
            runSpinAnimation(
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
        val category = cwIndexToCategory(selected.categoryIndex, state.categories) ?: return
        viewModelScope.launch {
            _events.emit(MandalartUiEvent.NavigateToMenuSelect(category.id))
        }
    }

    fun resetSpin() {
        _uiState.update { it.copy(spinState = SpinState.Idle, highlightedIndex = -1) }
    }

    private fun cwIndexToCategory(cwIndex: Int, categories: List<Category>): Category? {
        if (cwIndex !in 0..7) return null
        val targetPosition = CW_TO_POSITION[cwIndex]
        return categories.firstOrNull { it.position == targetPosition }
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
