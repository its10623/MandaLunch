package com.example.mandalunch.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mandalunch.domain.model.Category
import com.example.mandalunch.domain.model.Menu
import com.example.mandalunch.domain.usecase.AddMenuToPoolUseCase
import com.example.mandalunch.domain.usecase.DeleteMenuUseCase
import com.example.mandalunch.domain.usecase.GetAllMenusByCategoryUseCase
import com.example.mandalunch.domain.usecase.GetCategoriesUseCase
import com.example.mandalunch.domain.usecase.RandomizeBoardMenusUseCase
import com.example.mandalunch.domain.usecase.UpdateBoardMenusUseCase
import com.example.mandalunch.presentation.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MenuEditUiState(
    val category: Category? = null,
    val boardMenus: List<Menu> = emptyList(),   // size <= 8
    val poolMenus: List<Menu> = emptyList(),
    val pendingMenuName: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false
) {
    val canSave: Boolean get() = boardMenus.size == 8 && !isLoading && !isSaving
    val canAddToBoard: Boolean get() = boardMenus.size < 8
}

sealed class MenuEditUiEvent {
    data object NavigateBack : MenuEditUiEvent()
    data class ShowToast(val message: String) : MenuEditUiEvent()
}

@HiltViewModel
class MenuEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getAllMenusByCategoryUseCase: GetAllMenusByCategoryUseCase,
    private val addMenuToPoolUseCase: AddMenuToPoolUseCase,
    private val deleteMenuUseCase: DeleteMenuUseCase,
    private val updateBoardMenusUseCase: UpdateBoardMenusUseCase,
    private val randomizeBoardMenusUseCase: RandomizeBoardMenusUseCase
) : ViewModel() {

    private val categoryId: Int =
        savedStateHandle.get<Int>(Routes.ARG_CATEGORY_ID) ?: -1

    /**
     * 편집 버퍼: 사용자가 토글한 보드 ID 집합.
     * null = 아직 편집 안 함 → DB의 isOnBoard 그대로 사용.
     * non-null = 편집 모드 → 이 셋에 든 menuId만 보드로 간주.
     */
    private val pendingBoardIds = MutableStateFlow<Set<Int>?>(null)

    private val _uiState = MutableStateFlow(MenuEditUiState())
    val uiState: StateFlow<MenuEditUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<MenuEditUiEvent>()
    val events: SharedFlow<MenuEditUiEvent> = _events.asSharedFlow()

    init {
        // race condition 방지: category를 먼저 동기 로드 후 combine 시작
        viewModelScope.launch {
            val cat = getCategoriesUseCase().first().firstOrNull { it.id == categoryId }
            _uiState.update { it.copy(category = cat) }
            observeMenus()
        }
    }

    private fun observeMenus() {
        viewModelScope.launch {
            combine(
                getAllMenusByCategoryUseCase(categoryId),
                pendingBoardIds
            ) { allMenus, pending ->
                val effectiveBoardIds: Set<Int> = pending
                    ?: allMenus.filter { it.isOnBoard }.map { it.id }.toSet()
                val board = allMenus.filter { it.id in effectiveBoardIds }
                val pool = allMenus.filter { it.id !in effectiveBoardIds }
                Pair(board, pool)
            }.collect { (board, pool) ->
                _uiState.update {
                    it.copy(
                        boardMenus = board,
                        poolMenus = pool,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onUpdatePendingName(name: String) {
        _uiState.update { it.copy(pendingMenuName = name) }
    }

    fun onAddMenu() {
        val name = _uiState.value.pendingMenuName.trim()
        if (name.isBlank()) return
        viewModelScope.launch {
            try {
                addMenuToPoolUseCase(name, categoryId)
                _uiState.update { it.copy(pendingMenuName = "") }
            } catch (e: IllegalArgumentException) {
                _events.emit(MenuEditUiEvent.ShowToast("메뉴 이름이 올바르지 않습니다"))
            }
        }
    }

    fun onDeleteMenu(menu: Menu) {
        // 풀 메뉴 한정 (UI에서도 풀 섹션에서만 노출). DB의 isOnBoard가 아닌 effective 상태로 판단.
        val effectiveBoardIds: Set<Int> = pendingBoardIds.value
            ?: _uiState.value.boardMenus.map { it.id }.toSet()
        if (menu.id in effectiveBoardIds) {
            viewModelScope.launch {
                _events.emit(MenuEditUiEvent.ShowToast("보드 메뉴는 풀로 내린 후 삭제하세요"))
            }
            return
        }
        // 전체 메뉴가 8개 미만이 되는 것 차단
        val totalAfterDelete = _uiState.value.boardMenus.size + _uiState.value.poolMenus.size - 1
        if (totalAfterDelete < 8) {
            viewModelScope.launch {
                _events.emit(MenuEditUiEvent.ShowToast("카테고리당 최소 8개 메뉴는 유지되어야 합니다"))
            }
            return
        }
        viewModelScope.launch {
            deleteMenuUseCase(menu)
        }
    }

    /**
     * 보드 ↔ 풀 토글.
     * - 보드에 있으면 → 풀로 이동 (pending에서 제거)
     * - 풀에 있으면 → 보드로 이동 (단, 현재 보드가 8개 미만일 때만)
     * 토글 결과는 로컬 pendingBoardIds로만 관리하며, DB 반영은 onSave()에서.
     */
    fun onToggleBoardMenu(menu: Menu) {
        val current: Set<Int> = pendingBoardIds.value
            ?: _uiState.value.boardMenus.map { it.id }.toSet()

        if (menu.id in current) {
            // 보드 → 풀
            pendingBoardIds.value = current - menu.id
        } else {
            // 풀 → 보드
            if (current.size >= 8) {
                viewModelScope.launch {
                    _events.emit(MenuEditUiEvent.ShowToast("보드는 최대 8개까지만 가능합니다"))
                }
                return
            }
            pendingBoardIds.value = current + menu.id
        }
    }

    fun onRandomize() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                randomizeBoardMenusUseCase(categoryId)
                // 사용자의 stale 토글이 DB 새 보드를 가리지 않도록 편집 버퍼 리셋
                pendingBoardIds.value = null
                _events.emit(MenuEditUiEvent.ShowToast("랜덤으로 8개 메뉴를 선택했습니다"))
            } catch (e: IllegalStateException) {
                _events.emit(MenuEditUiEvent.ShowToast("메뉴가 8개 이상이어야 랜덤 채우기 가능합니다"))
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun onSave() {
        val pending = pendingBoardIds.value
        if (pending == null) {
            // 편집 사항 없음 → 그냥 뒤로
            viewModelScope.launch { _events.emit(MenuEditUiEvent.NavigateBack) }
            return
        }
        if (pending.size != 8) {
            viewModelScope.launch {
                _events.emit(
                    MenuEditUiEvent.ShowToast("보드 메뉴는 정확히 8개여야 합니다 (현재 ${pending.size}개)")
                )
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                updateBoardMenusUseCase(categoryId, pending.toList())
                pendingBoardIds.value = null
                _events.emit(MenuEditUiEvent.NavigateBack)
            } catch (e: IllegalArgumentException) {
                _events.emit(MenuEditUiEvent.ShowToast("저장 실패: ${e.message ?: "알 수 없는 오류"}"))
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun onCancelBack() {
        viewModelScope.launch { _events.emit(MenuEditUiEvent.NavigateBack) }
    }
}
