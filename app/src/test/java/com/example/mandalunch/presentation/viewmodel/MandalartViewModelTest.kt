package com.example.mandalunch.presentation.viewmodel

import app.cash.turbine.test
import com.example.mandalunch.domain.model.Category
import com.example.mandalunch.domain.model.Menu
import com.example.mandalunch.domain.usecase.GetCategoriesUseCase
import com.example.mandalunch.domain.usecase.GetBoardMenusByCategoryUseCase
import com.example.mandalunch.testutil.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MandalartViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getCategoriesUseCase: GetCategoriesUseCase = mockk(relaxed = true)
    private val getBoardMenusByCategoryUseCase: GetBoardMenusByCategoryUseCase = mockk(relaxed = true)

    // CW_TO_POSITION = [0,1,2,4,7,6,5,3]
    // CW index 0..7 → position 0..7 매핑
    private val categories8 = listOf(
        Category(id = 1, name = "한식", emoji = "🍚", position = 0),
        Category(id = 2, name = "중식", emoji = "🥢", position = 1),
        Category(id = 3, name = "일식", emoji = "🍣", position = 2),
        Category(id = 4, name = "아시안", emoji = "🌶️", position = 4),
        Category(id = 5, name = "건강식", emoji = "🥗", position = 7),
        Category(id = 6, name = "프랜차이즈", emoji = "🍔", position = 6),
        Category(id = 7, name = "분식", emoji = "🍢", position = 5),
        Category(id = 8, name = "양식", emoji = "🍝", position = 3)
    )

    private fun menuListFor(categoryId: Int): List<Menu> =
        (1..8).map { Menu(id = categoryId * 100 + it, name = "m$it", categoryId = categoryId) }

    private fun createViewModel(): MandalartViewModel =
        MandalartViewModel(getCategoriesUseCase, getBoardMenusByCategoryUseCase)

    @Test
    fun init_loadsAllCategoriesAndMenusMap() = runTest {
        every { getCategoriesUseCase() } returns flowOf(categories8)
        categories8.forEach { cat ->
            every { getBoardMenusByCategoryUseCase(cat.id) } returns flowOf(menuListFor(cat.id))
        }

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(8, state.categories.size)
        assertEquals(8, state.menusByCategoryId.size)
        categories8.forEach { cat ->
            assertEquals(8, state.menusByCategoryId[cat.id]?.size)
        }
    }

    @Test
    fun init_emptyCategories_doesNotCallMenusUseCase() = runTest {
        every { getCategoriesUseCase() } returns flowOf(emptyList())

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.categories.isEmpty())
        assertTrue(viewModel.uiState.value.menusByCategoryId.isEmpty())
        verify(exactly = 0) { getBoardMenusByCategoryUseCase(any()) }
    }

    @Test
    fun onSpinClick_normal_endsInSelectedState() = runTest {
        every { getCategoriesUseCase() } returns flowOf(categories8)
        categories8.forEach { cat ->
            every { getBoardMenusByCategoryUseCase(cat.id) } returns flowOf(menuListFor(cat.id))
        }

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.randomProvider = { 3 }
        viewModel.onSpinClick()

        assertEquals(SpinState.Spinning, viewModel.uiState.value.spinState)
        assertEquals(-1, viewModel.uiState.value.highlightedIndex)

        advanceUntilIdle()

        assertEquals(SpinState.Selected(3), viewModel.uiState.value.spinState)
        assertEquals(3, viewModel.uiState.value.highlightedIndex)
    }

    @Test
    fun onSpinClick_fewerThan8Categories_noStateChange() = runTest {
        val few = categories8.take(3)
        every { getCategoriesUseCase() } returns flowOf(few)
        few.forEach { cat ->
            every { getBoardMenusByCategoryUseCase(cat.id) } returns flowOf(menuListFor(cat.id))
        }

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onSpinClick()
        advanceUntilIdle()

        assertEquals(SpinState.Idle, viewModel.uiState.value.spinState)
    }

    @Test
    fun onSpinClick_duplicateCall_secondCallIgnored() = runTest {
        every { getCategoriesUseCase() } returns flowOf(categories8)
        categories8.forEach { cat ->
            every { getBoardMenusByCategoryUseCase(cat.id) } returns flowOf(menuListFor(cat.id))
        }

        val viewModel = createViewModel()
        advanceUntilIdle()

        var callCount = 0
        viewModel.randomProvider = { callCount++; 3 }

        viewModel.onSpinClick()
        // Spinning 상태에서 두 번째 onSpinClick 호출
        viewModel.onSpinClick()

        advanceUntilIdle()

        // randomProvider는 첫 호출에서만 실행되어야 함 (가드 검증)
        assertEquals(1, callCount)
    }

    @Test
    fun onGoToMenuClick_selectedState_emitsWithMappedCategoryId() = runTest {
        every { getCategoriesUseCase() } returns flowOf(categories8)
        categories8.forEach { cat ->
            every { getBoardMenusByCategoryUseCase(cat.id) } returns flowOf(menuListFor(cat.id))
        }

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.randomProvider = { 3 }  // CW index 3
        viewModel.onSpinClick()
        advanceUntilIdle()

        // CW index 3 → CW_TO_POSITION[3] = 4 → position 4의 카테고리 = 아시안(id=4)
        val expectedCategoryId = categories8.first { it.position == 4 }.id

        viewModel.events.test {
            viewModel.onGoToMenuClick()
            advanceUntilIdle()
            val event = awaitItem()
            assertTrue(event is MandalartUiEvent.NavigateToMenuSelect)
            assertEquals(expectedCategoryId, event.categoryId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onGoToMenuClick_idleState_noEvent() = runTest {
        every { getCategoriesUseCase() } returns flowOf(categories8)
        categories8.forEach { cat ->
            every { getBoardMenusByCategoryUseCase(cat.id) } returns flowOf(menuListFor(cat.id))
        }

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onGoToMenuClick()
            advanceUntilIdle()
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun resetSpin_returnsToIdle() = runTest {
        every { getCategoriesUseCase() } returns flowOf(categories8)
        categories8.forEach { cat ->
            every { getBoardMenusByCategoryUseCase(cat.id) } returns flowOf(menuListFor(cat.id))
        }

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.randomProvider = { 3 }
        viewModel.onSpinClick()
        advanceUntilIdle()

        viewModel.resetSpin()

        assertEquals(SpinState.Idle, viewModel.uiState.value.spinState)
        assertEquals(-1, viewModel.uiState.value.highlightedIndex)
    }
}
