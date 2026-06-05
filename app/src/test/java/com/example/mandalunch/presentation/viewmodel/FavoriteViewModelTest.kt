package com.example.mandalunch.presentation.viewmodel

import app.cash.turbine.test
import com.example.mandalunch.domain.model.Category
import com.example.mandalunch.domain.model.Menu
import com.example.mandalunch.domain.usecase.ClearAllFavoritesUseCase
import com.example.mandalunch.domain.usecase.GetCategoriesUseCase
import com.example.mandalunch.domain.usecase.GetFavoritesUseCase
import com.example.mandalunch.domain.usecase.ToggleFavoriteUseCase
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
class FavoriteViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getFavoritesUseCase: GetFavoritesUseCase = mockk(relaxed = true)
    private val getCategoriesUseCase: GetCategoriesUseCase = mockk(relaxed = true)
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase = mockk(relaxed = true)
    private val clearAllFavoritesUseCase: ClearAllFavoritesUseCase = mockk(relaxed = true)

    private fun createViewModel(): FavoriteViewModel = FavoriteViewModel(
        getFavoritesUseCase,
        getCategoriesUseCase,
        toggleFavoriteUseCase,
        clearAllFavoritesUseCase
    )

    private val categories = listOf(
        Category(id = 1, name = "한식", emoji = "🍚", position = 0),
        Category(id = 2, name = "중식", emoji = "🥢", position = 1),
        Category(id = 3, name = "일식", emoji = "🍣", position = 2),
        Category(id = 4, name = "아시안", emoji = "🌶️", position = 3)
    )

    @Test
    fun init_emptyFavorites_setsIsEmptyTrue() = runTest {
        every { getFavoritesUseCase() } returns flowOf(emptyList())
        every { getCategoriesUseCase() } returns flowOf(categories)

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.sections.isEmpty())
        assertEquals(false, state.isLoading)
        assertEquals(true, state.isEmpty)
    }

    @Test
    fun init_groupsByCategory() = runTest {
        val favorites = listOf(
            Menu(id = 1, name = "비빔밥", categoryId = 1, isFavorite = true),
            Menu(id = 2, name = "짜장면", categoryId = 2, isFavorite = true)
        )
        every { getFavoritesUseCase() } returns flowOf(favorites)
        every { getCategoriesUseCase() } returns flowOf(categories)

        val viewModel = createViewModel()
        advanceUntilIdle()

        val sections = viewModel.uiState.value.sections
        assertEquals(2, sections.size)
        assertEquals("한식", sections[0].categoryName)
        assertEquals("중식", sections[1].categoryName)
    }

    @Test
    fun init_sectionsOrderedByCategoryPositionAscending() = runTest {
        val favorites = listOf(
            // 아시안(pos=3), 한식(pos=0), 중식(pos=1) 순서로 섞어 입력
            Menu(id = 10, name = "팟타이", categoryId = 4, isFavorite = true),
            Menu(id = 11, name = "비빔밥", categoryId = 1, isFavorite = true),
            Menu(id = 12, name = "짜장면", categoryId = 2, isFavorite = true)
        )
        every { getFavoritesUseCase() } returns flowOf(favorites)
        every { getCategoriesUseCase() } returns flowOf(categories)

        val viewModel = createViewModel()
        advanceUntilIdle()

        val positions = viewModel.uiState.value.sections.map { it.categoryPosition }
        assertEquals(listOf(0, 1, 3), positions)
    }

    @Test
    fun init_withinSection_menusOrderedByNameAscending() = runTest {
        val favorites = listOf(
            Menu(id = 3, name = "짜장면", categoryId = 2, isFavorite = true),
            Menu(id = 1, name = "가츠동", categoryId = 2, isFavorite = true),
            Menu(id = 2, name = "라멘", categoryId = 2, isFavorite = true)
        )
        every { getFavoritesUseCase() } returns flowOf(favorites)
        every { getCategoriesUseCase() } returns flowOf(categories)

        val viewModel = createViewModel()
        advanceUntilIdle()

        val sections = viewModel.uiState.value.sections
        assertEquals(1, sections.size)
        assertEquals(listOf("가츠동", "라멘", "짜장면"), sections[0].menus.map { it.name })
    }

    @Test
    fun init_menuWithUnknownCategory_isExcluded() = runTest {
        val favorites = listOf(
            Menu(id = 99, name = "유령음식", categoryId = 99, isFavorite = true)
        )
        every { getFavoritesUseCase() } returns flowOf(favorites)
        every { getCategoriesUseCase() } returns flowOf(categories)

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.sections.isEmpty())
    }

    @Test
    fun onBackClick_emitsNavigateBack() = runTest {
        every { getFavoritesUseCase() } returns flowOf(emptyList())
        every { getCategoriesUseCase() } returns flowOf(categories)

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onBackClick()
            advanceUntilIdle()
            assertEquals(FavoriteUiEvent.NavigateBack, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onToggleFavorite_callsToggleFavoriteUseCaseWithOpposite() = runTest {
        every { getFavoritesUseCase() } returns flowOf(emptyList())
        every { getCategoriesUseCase() } returns flowOf(categories)

        val viewModel = createViewModel()
        advanceUntilIdle()

        val menu = Menu(id = 5, name = "초밥", categoryId = 3, isFavorite = true)
        viewModel.onToggleFavorite(menu)
        advanceUntilIdle()

        coVerify(exactly = 1) { toggleFavoriteUseCase(5, false) }
    }

    @Test
    fun onClearAllClick_setsShowClearConfirmTrue() = runTest {
        every { getFavoritesUseCase() } returns flowOf(emptyList())
        every { getCategoriesUseCase() } returns flowOf(categories)

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onClearAllClick()

        assertEquals(true, viewModel.uiState.value.showClearConfirm)
    }

    @Test
    fun onDismissClearConfirm_setsShowClearConfirmFalse() = runTest {
        every { getFavoritesUseCase() } returns flowOf(emptyList())
        every { getCategoriesUseCase() } returns flowOf(categories)

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onClearAllClick()
        viewModel.onDismissClearConfirm()

        assertEquals(false, viewModel.uiState.value.showClearConfirm)
    }

    @Test
    fun onConfirmClearAll_callsUseCaseAndEmitsClearAllCompleted() = runTest {
        every { getFavoritesUseCase() } returns flowOf(emptyList())
        every { getCategoriesUseCase() } returns flowOf(categories)

        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onClearAllClick()

        viewModel.events.test {
            viewModel.onConfirmClearAll()
            advanceUntilIdle()
            assertEquals(FavoriteUiEvent.ClearAllCompleted, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { clearAllFavoritesUseCase() }
        assertEquals(false, viewModel.uiState.value.showClearConfirm)
    }
}
