package com.example.mandalunch.presentation.viewmodel

import app.cash.turbine.test
import com.example.mandalunch.domain.model.RecommendHistory
import com.example.mandalunch.domain.usecase.DeleteAllHistoryUseCase
import com.example.mandalunch.domain.usecase.GetCategoriesUseCase
import com.example.mandalunch.domain.usecase.GetHistoryUseCase
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getHistoryUseCase: GetHistoryUseCase = mockk(relaxed = true)
    private val getCategoriesUseCase: GetCategoriesUseCase = mockk(relaxed = true)
    private val deleteAllHistoryUseCase: DeleteAllHistoryUseCase = mockk(relaxed = true)

    private fun createViewModel(): HistoryViewModel {
        every { getCategoriesUseCase() } returns flowOf(emptyList())
        return HistoryViewModel(getHistoryUseCase, getCategoriesUseCase, deleteAllHistoryUseCase)
    }

    private val sampleHistories = listOf(
        RecommendHistory(
            id = 1,
            menuId = 1,
            menuName = "비빔밥",
            categoryName = "한식",
            recommendedAt = System.currentTimeMillis()
        ),
        RecommendHistory(
            id = 2,
            menuId = 2,
            menuName = "짜장면",
            categoryName = "중식",
            recommendedAt = System.currentTimeMillis() - 3_600_000L
        )
    )

    @Test
    fun init_withHistories_loadsAndIsNotEmpty() = runTest {
        every { getHistoryUseCase() } returns flowOf(sampleHistories)

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isEmpty)
        assertTrue(state.sections.isNotEmpty())
    }

    @Test
    fun init_emptyHistories_setsIsEmptyTrue() = runTest {
        every { getHistoryUseCase() } returns flowOf(emptyList())

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isEmpty)
        assertTrue(state.sections.isEmpty())
    }

    @Test
    fun onBackClick_emitsNavigateBack() = runTest {
        every { getHistoryUseCase() } returns flowOf(emptyList())

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onBackClick()
            advanceUntilIdle()
            assertEquals(HistoryUiEvent.NavigateBack, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onDeleteAllClick_setsShowDeleteConfirmTrue() = runTest {
        every { getHistoryUseCase() } returns flowOf(emptyList())

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onDeleteAllClick()

        assertTrue(viewModel.uiState.value.showDeleteConfirm)
    }

    @Test
    fun onDismissDeleteConfirm_setsShowDeleteConfirmFalse() = runTest {
        every { getHistoryUseCase() } returns flowOf(emptyList())

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onDeleteAllClick()
        viewModel.onDismissDeleteConfirm()

        assertFalse(viewModel.uiState.value.showDeleteConfirm)
    }

    @Test
    fun onConfirmDeleteAll_callsUseCaseAndEmitsCompleted() = runTest {
        every { getHistoryUseCase() } returns flowOf(emptyList())

        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onDeleteAllClick()

        viewModel.events.test {
            viewModel.onConfirmDeleteAll()
            advanceUntilIdle()
            assertEquals(HistoryUiEvent.DeleteAllCompleted, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { deleteAllHistoryUseCase() }
        assertFalse(viewModel.uiState.value.showDeleteConfirm)
    }
}
