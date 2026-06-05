package com.example.mandalunch.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.example.mandalunch.domain.model.Category
import com.example.mandalunch.domain.model.Menu
import com.example.mandalunch.domain.repository.MenuRepository
import com.example.mandalunch.domain.usecase.GetCategoriesUseCase
import com.example.mandalunch.domain.usecase.GetMenusByCategoryUseCase
import com.example.mandalunch.domain.usecase.SaveHistoryUseCase
import com.example.mandalunch.domain.usecase.ToggleFavoriteUseCase
import com.example.mandalunch.presentation.navigation.Routes
import com.example.mandalunch.testutil.MainDispatcherRule
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MenuSelectViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getCategoriesUseCase: GetCategoriesUseCase = mockk(relaxed = true)
    private val getMenusByCategoryUseCase: GetMenusByCategoryUseCase = mockk(relaxed = true)
    private val saveHistoryUseCase: SaveHistoryUseCase = mockk(relaxed = true)
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase = mockk(relaxed = true)
    private val menuRepository: MenuRepository = mockk(relaxed = true)

    private val categoryOne = Category(id = 1, name = "한식", emoji = "🍚", position = 0)
    private val categories = listOf(categoryOne)

    private val menusForCat1 = (1..8).map { i ->
        Menu(id = i, name = "메뉴$i", categoryId = 1, isFavorite = false)
    }

    private fun savedStateForCat(id: Int): SavedStateHandle =
        SavedStateHandle(mapOf(Routes.ARG_CATEGORY_ID to id))

    private fun createViewModel(
        savedStateHandle: SavedStateHandle = savedStateForCat(1)
    ): MenuSelectViewModel = MenuSelectViewModel(
        savedStateHandle,
        getCategoriesUseCase,
        getMenusByCategoryUseCase,
        saveHistoryUseCase,
        toggleFavoriteUseCase,
        menuRepository
    )

    @Test
    fun init_loadsCategoryAndMenus() = runTest {
        every { getCategoriesUseCase() } returns flowOf(categories)
        every { getMenusByCategoryUseCase(1) } returns flowOf(menusForCat1)

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.category?.id)
        assertEquals(8, state.menus.size)
    }

    @Test
    fun init_sortsByFavoriteDescThenIdAsc() = runTest {
        val unsorted = listOf(
            Menu(id = 3, name = "m3", categoryId = 1, isFavorite = false),
            Menu(id = 1, name = "m1", categoryId = 1, isFavorite = true),
            Menu(id = 2, name = "m2", categoryId = 1, isFavorite = true),
            Menu(id = 4, name = "m4", categoryId = 1, isFavorite = false)
        )
        every { getCategoriesUseCase() } returns flowOf(categories)
        every { getMenusByCategoryUseCase(1) } returns flowOf(unsorted)

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(listOf(1, 2, 3, 4), viewModel.uiState.value.menus.map { it.id })
    }

    @Test
    fun onToggleFavorite_idleState_callsUseCase() = runTest {
        every { getCategoriesUseCase() } returns flowOf(categories)
        every { getMenusByCategoryUseCase(1) } returns flowOf(menusForCat1)

        val viewModel = createViewModel()
        advanceUntilIdle()

        val menu = Menu(id = 5, name = "m5", categoryId = 1, isFavorite = false)
        viewModel.onToggleFavorite(menu)
        advanceUntilIdle()

        coVerify(exactly = 1) { toggleFavoriteUseCase(5, true) }
    }

    @Test
    fun onToggleFavorite_spinningState_doesNotCallUseCase() = runTest {
        every { getCategoriesUseCase() } returns flowOf(categories)
        every { getMenusByCategoryUseCase(1) } returns flowOf(menusForCat1)

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.randomProvider = { 5 }
        viewModel.onSpinClick()
        // advanceUntilIdle 호출 전: 아직 Spinning 상태 - 그러나 첫 step의 highlight가 적용 후 delay에 진입
        // 즉시 onToggleFavorite 호출 → 가드 동작 검증

        val menu = Menu(id = 5, name = "m5", categoryId = 1, isFavorite = false)
        viewModel.onToggleFavorite(menu)

        coVerify(exactly = 0) { toggleFavoriteUseCase(any(), any()) }

        // 정리: 스핀 종료
        advanceUntilIdle()
    }

    @Test
    fun onSpinClick_normal_endsInSelectedState() = runTest {
        every { getCategoriesUseCase() } returns flowOf(categories)
        every { getMenusByCategoryUseCase(1) } returns flowOf(menusForCat1)

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.randomProvider = { 5 }
        viewModel.onSpinClick()

        // 즉시 검증: Spinning
        // 단, StandardTestDispatcher 환경에서 onSpinClick은 viewModelScope.launch를 큐에 등록하므로
        // uiState 변경(spinState=Spinning, highlightedIndex=-1)은 onSpinClick 내부에서 동기적으로 발생.
        assertEquals(MenuSpinState.Spinning, viewModel.uiState.value.spinState)
        assertEquals(-1, viewModel.uiState.value.highlightedIndex)

        advanceUntilIdle()

        assertEquals(MenuSpinState.Selected(5), viewModel.uiState.value.spinState)
        assertEquals(5, viewModel.uiState.value.highlightedIndex)
    }

    @Test
    fun onSpinClick_menusLessThan8_noStateChange() = runTest {
        val fewMenus = (1..3).map { Menu(id = it, name = "m$it", categoryId = 1) }
        every { getCategoriesUseCase() } returns flowOf(categories)
        every { getMenusByCategoryUseCase(1) } returns flowOf(fewMenus)

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onSpinClick()
        advanceUntilIdle()

        assertEquals(MenuSpinState.Idle, viewModel.uiState.value.spinState)
    }

    @Test
    fun onGoToResult_selectedState_savesAndEmits() = runTest {
        every { getCategoriesUseCase() } returns flowOf(categories)
        every { getMenusByCategoryUseCase(1) } returns flowOf(menusForCat1)

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.randomProvider = { 5 }
        viewModel.onSpinClick()
        advanceUntilIdle()

        val selectedMenu = menusForCat1[5]

        viewModel.events.test {
            viewModel.onGoToResult()
            advanceUntilIdle()
            val event = awaitItem()
            assertTrue(event is MenuSelectUiEvent.NavigateToResult)
            assertEquals(1, event.categoryId)
            assertEquals(selectedMenu.name, event.menuName)
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) {
            menuRepository.updateLastRecommended(selectedMenu.id, match { it > 0L })
        }
        coVerify(exactly = 1) {
            saveHistoryUseCase(
                match {
                    it.menuName == selectedMenu.name &&
                        it.categoryName == "한식" &&
                        it.menuId == selectedMenu.id
                }
            )
        }
    }

    @Test
    fun onGoToResult_idleState_noopAndNoEvent() = runTest {
        every { getCategoriesUseCase() } returns flowOf(categories)
        every { getMenusByCategoryUseCase(1) } returns flowOf(menusForCat1)

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onGoToResult()
            advanceUntilIdle()
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 0) { saveHistoryUseCase(any()) }
    }

    @Test
    fun resetSpin_clearsToIdle() = runTest {
        every { getCategoriesUseCase() } returns flowOf(categories)
        every { getMenusByCategoryUseCase(1) } returns flowOf(menusForCat1)

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.randomProvider = { 5 }
        viewModel.onSpinClick()
        advanceUntilIdle()

        viewModel.resetSpin()

        assertEquals(MenuSpinState.Idle, viewModel.uiState.value.spinState)
        assertEquals(-1, viewModel.uiState.value.highlightedIndex)
    }
}
